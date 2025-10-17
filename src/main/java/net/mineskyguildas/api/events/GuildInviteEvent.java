package net.mineskyguildas.api.events;

import net.mineskyguildas.data.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuildInviteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Player inviter;
    private final Player invited;
    private final Guilds guild;

    public String CancelledMessage;

    public GuildInviteEvent(Player inviter, Player invited, Guilds guild) {
        this.inviter = inviter;
        this.invited = invited;
        this.guild = guild;
    }

    public Player getInviter() {
        return inviter;
    }

    public Player getInvited() {
        return inviter;
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
