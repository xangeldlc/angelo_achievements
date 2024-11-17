package com.xangeldlc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public Set<String> getAchievementKeys() {
        if (getConfig().isConfigurationSection("achievements")) {
            return getConfig().getConfigurationSection("achievements").getKeys(false);
        }
        return null;
    }

    public String getDefaultMessage() {
        return getConfig().getString("default_message", "<gray>Default achievement message!</gray>");
    }
}
