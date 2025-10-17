package net.mineskyguildas.api.events.playerevents;

import net.mineskyguildas.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerLeaveGuildEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Guilds guild;

    public String CancelledMessage;


    public PlayerLeaveGuildEvent(Player who, Guilds guild) {
        super(who);
        this.guild = guild;
    }

    public Guilds getGuild() {
        return guild;
    }

    public void setCancelledMessage(String message) {
        CancelledMessage = message;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
