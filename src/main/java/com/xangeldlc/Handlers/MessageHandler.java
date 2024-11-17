package com.xangeldlc.Handlers;

import com.xangeldlc.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MessageHandler extends BaseHandler {

    private final BukkitAudiences adventure;

    // Enum que define los comandos personalizados
    public enum CommandType {
        BROADCAST("[BROADCAST]"),
        SOUND("[SOUND]");

        private final String prefix;

        CommandType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public static Optional<CommandType> fromCommand(String command) {
            return Arrays.stream(values())
                    .filter(type -> command.startsWith(type.getPrefix()))
                    .findFirst();
        }
    }


    public MessageHandler(BukkitAudiences adventure) {
        this.adventure = adventure;
    }

    @Override
    public void handle(PlayerAdvancementDoneEvent event, ConfigManager configManager) throws Exception {
        FileConfiguration config = configManager.getConfig();
        String advancementKey = event.getAdvancement().getKey().toString();
        Player player = event.getPlayer();

        // Ignore achievements of type "minecraft:recipes"
        if (isRecipeAdvancement(advancementKey)) return;

        if (!handleCustomAchievement(config, advancementKey, player)) {
            sendDefaultMessage(player, configManager.getDefaultMessage(), player.getName(), advancementKey);
        }

        super.handle(event, configManager);
    }

    private boolean handleCustomAchievement(FileConfiguration config, String advancementKey, Player player) {
        if (!isValidConfigSection(config, "achievements")) return false;

        return config.getConfigurationSection("achievements").getKeys(false).stream()
                .filter(achievement -> isMatchingAdvancement(config, achievement, advancementKey))
                .findFirst()
                .map(achievement -> {
                    sendCustomMessages(config, achievement, player);
                    showCustomTitle(config, achievement, player);
                    executeCustomCommands(config, achievement, player);
                    return true;
                })
                .orElse(false);
    }

    private void sendCustomMessages(FileConfiguration config, String achievement, Player player) {
        List<String> messages = getListOrEmpty(config, "achievements." + achievement + ".message");
        messages.forEach(message -> sendMessageToPlayer(message, player, achievement));
    }

    private void showCustomTitle(FileConfiguration config, String achievement, Player player) {
        String title = getStringOrEmpty(config, "achievements." + achievement + ".title");
        String subtitle = getStringOrEmpty(config, "achievements." + achievement + ".subtitle");

        if (!title.isEmpty() || !subtitle.isEmpty()) {
            Component titleComponent = parseMessage(title, player, achievement);
            Component subtitleComponent = parseMessage(subtitle, player, achievement);
            adventure.player(player).showTitle(net.kyori.adventure.title.Title.title(titleComponent, subtitleComponent));
        }
    }

    private void executeCustomCommands(FileConfiguration config, String achievement, Player player) {
        List<String> commands = getListOrEmpty(config, "achievements." + achievement + ".commands");

        for (String command : commands) {
            CommandType.fromCommand(command).ifPresentOrElse(
                    type -> executeCommand(type, command, player, achievement),
                    () -> executeRawCommand(command, player, achievement)
            );
        }
    }

    private void executeCommand(CommandType type, String command, Player player, String achievement) {
        switch (type) {
            case BROADCAST -> {
                String message = command.replace(CommandType.BROADCAST.getPrefix(), "").trim();
                adventure.all().sendMessage(parseMessage(message, player, message));
            }
            case SOUND -> {
                playSoundCommand(command, player);
            }
        }
    }

    private void executeRawCommand(String command, Player player, String achievement) {
    String processedCommand = replacePlaceholders(command, player.getName(), achievement);

    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    
    }


    private void playSoundCommand(String command, Player player) {
        String[] soundParts = command.replace(CommandType.SOUND.getPrefix(), "").trim().split(" ");
        if (soundParts.length < 1) return;

        try {
            Sound sound = Sound.valueOf(soundParts[0].toUpperCase());
            float volume = soundParts.length >= 2 ? Float.parseFloat(soundParts[1]) : 1.0f;
            float pitch = soundParts.length >= 3 ? Float.parseFloat(soundParts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            logError("Invalid sound command: " + command);
        }
    }

    private void sendMessageToPlayer(String message, Player player, String achievement) {
        Component parsedMessage = parseMessage(message, player, achievement);
        adventure.player(player).sendMessage(parsedMessage);
    }

    private Component parseMessage(String message, Player player, String achievement) {
        String processedMessage = replacePlaceholders(message, player.getName(), achievement);
        return MiniMessage.miniMessage().deserialize(processedMessage);
    }    

    private String replacePlaceholders(String message, String playerName, String achievement) {
        return message.replace("%player%", playerName).replace("%achievements%", formatAdvancement(achievement));
    }

    private String formatAdvancement(String advancementKey) {
        if (advancementKey == null || advancementKey.isEmpty()) return "";
        String[] parts = advancementKey.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1).toLowerCase();
    }

    private void sendDefaultMessage(Player player, String defaultMessage, String playerName, String advancementKey) {
        if (defaultMessage == null || defaultMessage.isEmpty()) return;
        sendMessageToPlayer(defaultMessage, player, advancementKey);
    }

    private boolean isRecipeAdvancement(String advancementKey) {
        return advancementKey.startsWith("minecraft:recipes");
    }

    private boolean isValidConfigSection(FileConfiguration config, String path) {
        return config.isConfigurationSection(path);
    }

    private boolean isMatchingAdvancement(FileConfiguration config, String achievement, String advancementKey) {
        return Optional.ofNullable(config.getString("achievements." + achievement + ".type"))
                .map(type -> type.equals(advancementKey))
                .orElse(false);
    }

    private List<String> getListOrEmpty(FileConfiguration config, String path) {
        return config.isList(path) ? config.getStringList(path) : List.of();
    }

    private String getStringOrEmpty(FileConfiguration config, String path) {
        return Optional.ofNullable(config.getString(path)).orElse("");
    }

    private void logError(String message) {
        Bukkit.getLogger().severe(message);
    }
}
