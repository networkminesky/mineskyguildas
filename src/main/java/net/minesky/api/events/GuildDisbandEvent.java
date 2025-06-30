package net.minesky.api.events;

import net.minesky.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildDisbandEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Player disbander;
    private final Guilds guild;

    public String CancelledMessage;

    public GuildDisbandEvent(Player disbander, Guilds guild) {
        this.disbander = disbander;
        this.guild = guild;
    }

    public Player getDisbander() {
        return disbander;
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
