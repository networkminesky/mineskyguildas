package net.mineskyguildas.listeners;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.Notice;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private final MineSkyGuildas plugin;

    public PlayerEvents(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player damager = null;

        if (e.getDamager() instanceof Player) {
            damager = (Player) e.getDamager();
        } else {
            Object shooter = null;
            try {
                shooter = e.getDamager().getClass().getMethod("getShooter").invoke(e.getDamager());
            } catch (Exception ignored) {}
            if (shooter instanceof Player) {
                damager = (Player) shooter;
            }
        }

        if (damager == null) return;

        Guilds g = GuildHandler.getGuildByPlayer(victim);
        if (!g.equals(GuildHandler.getGuildByPlayer(damager))) return;

        if (!g.getFriendlyFire()) {
            e.setCancelled(true);
            damager.sendMessage("§c⚠ Fogo amigo está desabilitado - você não pode ferir membros da sua guilda.");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player killed = e.getEntity();
        Player killer = killed.getKiller();

       // plugin.getPlayerData().addDeath(killed.getUniqueId(), 1);
        if (killer == null) return;

    //    plugin.getPlayerData().addKill(killer.getUniqueId(), 1);

        Guilds killedGuild = GuildHandler.getGuildByPlayer(killed.getUniqueId());
        Guilds killerGuild = GuildHandler.getGuildByPlayer(killer.getUniqueId());

        if (killedGuild == null || killerGuild == null) return;

        if (killerGuild.equals(killedGuild) || killerGuild.isAlly(killedGuild)) return;

        GuildHandler.addKill(killer, killerGuild);
        GuildHandler.getMemberPromoteKills(killer, killerGuild);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Monster)) return;

        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        Guilds guild = GuildHandler.getGuildByPlayer(killer.getUniqueId());
        if (guild != null) {
            GuildHandler.addXpToGuild(killer.getUniqueId(), 1.0);
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7☠ &4+&c1 XP &4para sua guilda por derrotar um mob hostil&c!")));
        }
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        if (!(e.getKiller() instanceof Player player)) return;

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null ) return;

        EntityType type = e.getEntity().getType();

        boolean isHostile = (type == EntityType.HUSK);

        if (isHostile) {
            GuildHandler.addXpToGuild(player.getUniqueId(), 10);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                   new TextComponent(Utils.c("&7☠ &4+&c10 XP &4para sua guilda por derrotar um mob hostil&c!")));
        } else if (type == EntityType.PIG) {
            GuildHandler.addXpToGuild(player.getUniqueId(), 5);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7☠ &4+&c5 XP &4para sua guilda por derrotar um mob pacífico&c!")));
        } else {
            GuildHandler.addXpToGuild(player.getUniqueId(), 10);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7☠ &4+&c10 XP &4para sua guilda por derrotar um mob hostil&c!")));
        }
    }
    // Sistema de quando o player finalizar a quest e ganhar xp (Implementar sistema de nível da quest também)
    /*@EventHandler
    public void onQuestFinish(QuestFinishEvent e) {
        Player player = e.getPlayer();

        Guilds guilds = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guilds != null) {
            GuildHandler.addXpToGuild(player.getUniqueId(), 1);
        }
    }*/
}
