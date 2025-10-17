package net.mineskyguildas.api.events;

import net.mineskyguildas.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildAddAllyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Guilds ownGuild;
    private final Guilds allyGuild;
    private final Player creator;
    private final Player accepter;

    public String CancelledMessage;

    public GuildAddAllyEvent(Guilds ownGuild, Guilds allyGuild, Player creator, Player accepter) {
        this.ownGuild = ownGuild;
        this.allyGuild = allyGuild;
        this.creator = creator;
        this.accepter = accepter;
    }

    public Guilds getAllyRequester() {
        return ownGuild;
    }

    public Guilds getAllyGuild() {
        return allyGuild;
    }

    public Player getCreator() {
        return creator;
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
