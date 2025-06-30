package net.minesky.api.events;

import net.minesky.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildRemoveRivalEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Guilds ownGuild;
    private final Guilds rivalGuild;
    private final Player remover;
    private final Player accepter;

    public String CancelledMessage;

    public GuildRemoveRivalEvent(Guilds ownGuild, Guilds rivalGuild, Player remover, Player accepter) {
        this.ownGuild = ownGuild;
        this.rivalGuild = rivalGuild;
        this.remover = remover;
        this.accepter = accepter;
    }

    public Guilds getGuildRemover() {
        return ownGuild;
    }

    public Guilds getGuildAccepter() {
        return rivalGuild;
    }

    public Player getRemover() {
        return remover;
    }

    public Player getAccepter() {
        return accepter;
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
