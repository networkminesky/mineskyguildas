package net.minesky.api.events;

import net.minesky.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildAddRivalEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Guilds ownGuild;
    private final Guilds rivalGuild;
    private final Player creator;

    public String CancelledMessage;

    public GuildAddRivalEvent(Guilds ownGuild, Guilds rivalGuild, Player creator) {
        this.ownGuild = ownGuild;
        this.rivalGuild = rivalGuild;
        this.creator = creator;
    }

    public Guilds getOwnGuild() {
        return ownGuild;
    }

    public Guilds getRivalGuild() {
        return rivalGuild;
    }

    public Player getCreator() {
        return creator;
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
