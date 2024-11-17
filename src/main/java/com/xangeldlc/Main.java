package com.xangeldlc;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import com.xangeldlc.Commands.CommandHandler;

public class Main extends JavaPlugin {

    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        try {
            getLogger().info("AngeloAchievements se está iniciando...");

            adventure = BukkitAudiences.create(this);

            saveDefaultConfig();
            ConfigManager configManager = new ConfigManager(this);

            getServer().getPluginManager().registerEvents(
                    new AdvancementListener(configManager, adventure),
                    this
            );

            this.getCommand("achievementreload").setExecutor(new CommandHandler(configManager));


            getLogger().info("AngeloAchievements se ha iniciado correctamente.");
        } catch (Exception e) {
            getLogger().severe("Error al iniciar AngeloAchievements: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this); 
        }
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            try {
                adventure.close();
                getLogger().info("AngeloAchievements se ha desactivado correctamente.");
            } catch (Exception e) {
                getLogger().severe("Error al cerrar Adventure: " + e.getMessage());
            }
        }
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
