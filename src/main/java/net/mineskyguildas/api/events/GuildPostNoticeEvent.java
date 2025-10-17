package net.mineskyguildas.api.events;

import net.mineskyguildas.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildPostNoticeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Player player;;
    private final Guilds guild;
    private final String message;

    public String CancelledMessage;

    public GuildPostNoticeEvent(Player player, Guilds guild, String message) {
        this.player = player;
        this.guild = guild;
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public Guilds getGuild() {
        return guild;
    }

    public String getMessage() {
        return message;
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
