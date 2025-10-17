package net.mineskyguildas.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.utils.Utils;
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
            case "balance":
                return String.valueOf(GuildHandler.getGuildByPlayer(player).getBalance());
            case "label":
                return (GuildHandler.hasGuild(player) ? Utils.getGuildTagWithRole(player.getUniqueId()) : "");
            default:
                return "";
        }
    }
}