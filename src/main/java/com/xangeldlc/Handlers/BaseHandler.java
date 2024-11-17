package com.xangeldlc.Handlers;

import com.xangeldlc.ConfigManager;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public abstract class BaseHandler {
    protected BaseHandler next;

    public BaseHandler setNext(BaseHandler next) {
        this.next = next;
        return next;
    }

    public void handle(PlayerAdvancementDoneEvent event, ConfigManager configManager) throws Exception {
        if (next != null) {
            next.handle(event, configManager);
        }
    }
}
