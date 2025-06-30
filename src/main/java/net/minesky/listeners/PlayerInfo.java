package net.minesky.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.minesky.MineSkyGuildas;
import net.minesky.data.Guilds;
import net.minesky.data.Notice;
import net.minesky.handlers.GuildHandler;
import net.minesky.handlers.requests.GuildRequestType;
import net.minesky.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerInfo implements Listener {
    private final MineSkyGuildas plugin;

    public PlayerInfo(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        // Se o jogador não tem guilda, não há pedidos de aliança/rivaldade para ele
        if (guild != null) {
            plugin.getRequestManager()
                    .getHandler(GuildRequestType.ALLY)
                    .handleLogin(player, guild);

            plugin.getRequestManager()
                    .getHandler(GuildRequestType.RIVAL)
                    .handleLogin(player, guild);

            List<Notice> notices = guild.getNoticeBoard();

            player.sendMessage(Utils.c("&e✉ &6Mural da Guilda &e" + guild.getName() + ":"));

            if (notices.isEmpty()) {
                player.sendMessage(Utils.c("&7(sem mensagens ainda)"));
            } else {
                for (int i = 0; i < notices.size(); i++) {
                    String message = notices.get(i).getMessage();
                    player.sendMessage(Utils.c("&7" + (i + 1) + ". " + message));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player killed = e.getEntity();
        Player killer = killed.getKiller();

        plugin.getPlayerData().addDeath(killed.getUniqueId(), 1);

        if (killer == null) return;

        plugin.getPlayerData().addKill(killer.getUniqueId(), 1);

        Guilds killedGuild = GuildHandler.getGuildByPlayer(killed.getUniqueId());
        Guilds killerGuild = GuildHandler.getGuildByPlayer(killer.getUniqueId());

        if (killedGuild == null || killerGuild == null || killedGuild.equals(killerGuild) || killedGuild.isAlly(killerGuild)) {
            return;
        }

        GuildHandler.addKill(killer, killerGuild);
    }
}
