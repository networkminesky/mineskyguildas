package net.minesky.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Player creator;
    private final String guildID;
    private final String displayName;
    private final String tag;

    public String CancelledMessage;

    public GuildCreateEvent(Player creator, String id, String displayName, String tag) {
        this.creator = creator;
        this.guildID = id;
        this.displayName = displayName;
        this.tag = tag;
    }

    public Player getCreator() {
        return creator;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTag() {
        return tag;
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
