package net.minesky.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.minesky.MineSkyGuildas;
import net.minesky.handlers.GuildHandler;
import net.minesky.data.Guilds;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildasPlaceholder extends PlaceholderExpansion {

    private final MineSkyGuildas plugin;

    public GuildasPlaceholder(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mineskyguildas";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zBrunoC (Bruno C.)";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) return "";

        switch (identifier) {
            case "name":
                return GuildHandler.getGuildName(guild.getId());
            case "tag":
                return GuildHandler.getGuildTag(guild.getId());
            case "level":
                return String.valueOf(GuildHandler.getGuildLevel(guild.getId()));
            case "xp":
                return String.valueOf(GuildHandler.getGuildXp(guild.getId()));
            default:
                return "";
        }
    }
}