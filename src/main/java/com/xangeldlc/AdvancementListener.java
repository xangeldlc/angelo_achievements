package com.xangeldlc;

import com.xangeldlc.Handlers.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {

    private final ConfigManager configManager;
    private final BaseHandler handlerChain;
    
    public AdvancementListener(ConfigManager configManager, BukkitAudiences adventure) {
        this.configManager = configManager;
        
        handlerChain = new MessageHandler(adventure);

    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        try {
            handlerChain.handle(event, configManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
