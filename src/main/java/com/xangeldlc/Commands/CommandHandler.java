package com.xangeldlc.Commands;

import com.xangeldlc.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {
    private final ConfigManager configManager;

    public CommandHandler(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }
        return showUsage(sender);
    }

    private boolean handleReload(CommandSender sender) {
        configManager.reloadConfig();
        sender.sendMessage("§aThe configuration has been successfully reloaded.");
        return true;
    }

    private boolean showUsage(CommandSender sender) {
        sender.sendMessage("§cUse: /achievementreload");
        return false;
    }
}
