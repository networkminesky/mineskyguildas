package net.minesky.api.events.playerevents;

import net.minesky.data.Guilds;
import net.minesky.enums.GuildChatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChatEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private final Guilds guild;
    private final String message;
    private final GuildChatType type;

    public String CancelledMessage;


    public PlayerChatEvent(Player who, Guilds guild, String message, GuildChatType type) {
        super(who);
        this.guild = guild;
        this.message = message;
        this.type = type;
    }

    public Guilds getGuild() {
        return guild;
    }

    public String getMessage() {
        return message;
    }

    public GuildChatType getType() {
        return type;
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