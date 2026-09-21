package net.mineskyguildas.handlers;

import net.minesky.mineskygameplay.locatorapi.LocatorAPI;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.war.WarSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildLocatorHandler {

    private final MineSkyGuildas plugin;

    public GuildLocatorHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    private LocatorAPI getAPI() {
        return LocatorAPI.get();
    }

    public void updateVisibility(Player p1, Player p2) {
        LocatorAPI api = getAPI();
        if (api == null || p1 == null || p2 == null || !p1.isOnline() || !p2.isOnline()) {
            return;
        }

        Guilds g1 = GuildHandler.getGuildByPlayer(p1.getUniqueId());
        Guilds g2 = GuildHandler.getGuildByPlayer(p2.getUniqueId());

        if (g1 != null && g2 != null && g1.getId().equals(g2.getId())) {
            api.resetPlayerVisibility(p1, p2);
            api.resetPlayerVisibility(p2, p1);
            api.showPlayer(p1, p2);
            api.showPlayer(p2, p1);
            return;
        }

        if (g1 != null && g2 != null && g1.isAlly(g2)) {
            api.resetPlayerVisibility(p1, p2);
            api.resetPlayerVisibility(p2, p1);
            api.showPlayer(p1, p2);
            api.showPlayer(p2, p1);
            return;
        }

        if (g1 != null && g2 != null && areEnemiesInWar(g1, g2)) {
            api.resetPlayerVisibility(p1, p2);
            api.resetPlayerVisibility(p2, p1);
            api.showPlayer(p1, p2);
            api.showPlayer(p2, p1);
            return;
        }

        api.hidePlayer(p1, p2);
        api.hidePlayer(p2, p1);
    }

    public boolean areEnemiesInWar(Guilds g1, Guilds g2) {
        if (g1 == null || g2 == null || plugin.getWarHandler() == null) {
            return false;
        }

        Optional<WarSession> sessionOpt = plugin.getWarHandler().getActiveWarByGuild(g1.getId());
        if (sessionOpt.isEmpty()) {
            return false;
        }

        WarSession session = sessionOpt.get();
        String id1 = g1.getId();
        String id2 = g2.getId();

        int side1 = 0;
        if (id1.equals(session.getGuild1().getId()) || session.getGuild1Supporters().contains(id1)) {
            side1 = 1;
        } else if (id1.equals(session.getGuild2().getId()) || session.getGuild2Supporters().contains(id1)) {
            side1 = 2;
        }

        if (side1 == 0) return false;

        int side2 = 0;
        if (id2.equals(session.getGuild1().getId()) || session.getGuild1Supporters().contains(id2)) {
            side2 = 1;
        } else if (id2.equals(session.getGuild2().getId()) || session.getGuild2Supporters().contains(id2)) {
            side2 = 2;
        }

        return side2 != 0 && side1 != side2;
    }

    public void updatePlayer(Player player) {
        LocatorAPI api = getAPI();
        if (api == null || player == null || !player.isOnline()) return;

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild != null) {
            api.addPlayerToGroup("guild_" + guild.getId(), player);
        } else {
            api.removePlayerFromGroup(player);
            api.clearPlayerOverrides(player);
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId()) && other.isOnline()) {
                updateVisibility(player, other);
            }
        }
    }

    public void updateWarSession(WarSession session) {
        if (session == null) return;

        Set<String> involvedGuilds = new HashSet<>();
        if (session.getGuild1() != null) involvedGuilds.add(session.getGuild1().getId());
        if (session.getGuild2() != null) involvedGuilds.add(session.getGuild2().getId());
        involvedGuilds.addAll(session.getGuild1Supporters());
        involvedGuilds.addAll(session.getGuild2Supporters());

        for (String gId : involvedGuilds) {
            Guilds g = GuildHandler.getGuildByID(gId);
            if (g != null) {
                updateGuild(g);
            }
        }
    }

    public void updateBetweenGuilds(Guilds g1, Guilds g2) {
        if (g1 == null || g2 == null) return;

        for (UUID u1 : g1.getMembers().keySet()) {
            Player p1 = Bukkit.getPlayer(u1);
            if (p1 == null || !p1.isOnline()) continue;

            for (UUID u2 : g2.getMembers().keySet()) {
                Player p2 = Bukkit.getPlayer(u2);
                if (p2 == null || !p2.isOnline()) continue;

                updateVisibility(p1, p2);
            }
        }
    }

    public void updateGuild(Guilds guild) {
        if (guild == null) return;
        for (UUID uuid : guild.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updatePlayer(player);
            }
        }
    }

    public void onPlayerLeaveGuild(Player player) {
        LocatorAPI api = getAPI();
        if (api == null || player == null) return;

        api.removePlayerFromGroup(player);
        api.clearPlayerOverrides(player);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId())) {
                api.hidePlayer(player, other);
                api.hidePlayer(other, player);
            }
        }
    }

    public void onGuildDeleted(Guilds guild) {
        LocatorAPI api = getAPI();
        if (api == null || guild == null) return;

        api.deleteGroup("guild_" + guild.getId());

        for (UUID uuid : guild.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                onPlayerLeaveGuild(p);
            }
        }
    }

    public void onPlayerQuit(Player player) {
        LocatorAPI api = getAPI();
        if (api == null || player == null) return;

        api.removePlayerFromGroup(player);
        api.clearPlayerOverrides(player);
    }

    public void syncAllOnlinePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p);
        }
    }
}