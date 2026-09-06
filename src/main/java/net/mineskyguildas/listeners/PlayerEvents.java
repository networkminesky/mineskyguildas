package net.mineskyguildas.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
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
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

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

            plugin.getRequestManager()
                    .getHandler(GuildRequestType.WAR)
                    .handleLogin(player, guild);

            List<Notice> notices = guild.getNoticeBoard();

            player.sendMessage(Utils.c("&e✉ &6Mural do clã &e" + guild.getName() + ":"));

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
        if (!(e.getEntity() instanceof Player damaged) || !(e.getDamager() instanceof Player damager)) {
            return;
        }

        Guilds damagedGuild = GuildHandler.getGuildByPlayer(damaged.getUniqueId());
        Guilds damagerGuild = GuildHandler.getGuildByPlayer(damager.getUniqueId());

        if (damagedGuild == null || damagerGuild == null) {
            return;
        }

        if (damagedGuild.getId().equals(damagerGuild.getId()) || damagedGuild.isAlly(damagerGuild)) {
            if (!damagedGuild.getFriendlyFire()) {
                e.setCancelled(true);
                damager.sendMessage(Utils.c("&cVocê não pode atacar membros do seu clã ou aliados!"));
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

        if (e.isCancelled()) return;
        if (killerGuild != null) {
            if (killerGuild.equals(killedGuild)) return;

            GuildHandler.addKill(killer, killerGuild);
            GuildHandler.getMemberPromoteKills(killer, killerGuild);

            if (killedGuild != null) {
                if (killedGuild.isAlly(killerGuild)) {
                    killerGuild.removeXP(20);
                    killer.sendMessage(Utils.c("&cSeu clã perdeu 20 XP por abater um aliado!"));
                    return;
                }
                if (killedGuild.isRival(killerGuild)) {
                    killerGuild.addXP(10);
                    killer.sendMessage(Utils.c("&a&lRIVAL ABATIDO! &7+15 XP para o seu clã."));

                    killedGuild.removeXP(10);
                    killed.sendMessage(Utils.c("&c&lRIVALIDADE! &7Seu clã perdeu 10 XP porque você morreu para um rival."));
                }
            }

            GuildHandler.addXpToGuild(killer.getUniqueId(), 5);
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7☠ &4+&c5 XP &4para seu clã por derrotar um jogador&c!")));
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            e.getEntity().setMetadata("spawned_mob", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();

        if (!(entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast || entity instanceof Phantom)) return;

        if (e.getEntity().hasMetadata("spawned_mob")) return;

        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        Guilds guild = GuildHandler.getGuildByPlayer(killer.getUniqueId());
        if (guild != null) {
            if (e.isCancelled()) return;
            if (entity instanceof Slime) {
                GuildHandler.addXpToGuild(killer.getUniqueId(), 0.1);
                killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(Utils.c("&7☠ &4+&c0.1 XP &4para seu clã por derrotar um mob hostil&c!")));
                return;
            }
            GuildHandler.addXpToGuild(killer.getUniqueId(), 1);
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7☠ &4+&c1 XP &4para seu clã por derrotar um mob hostil&c!")));
        }
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        if (!(e.getKiller() instanceof Player player)) return;

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) return;

        if (e.getMob().getType().getHealth().get() >= 100.0) {
            GuildHandler.addXpToGuild(player.getUniqueId(), 5);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                   new TextComponent(Utils.c("&7☠ &4+&c5 XP &4para seu clã por derrotar um mob custom do servidor&c!")));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getType().name().endsWith("ORE")) {
            e.getBlock().setMetadata("block_placed", new FixedMetadataValue(plugin, true));
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());

        if (guild == null) return;

        if (e.getBlock().hasMetadata("block_placed")) {
            return;
        }

        if (e.getBlock().getType().name().endsWith("ORE")) {
            if (e.isCancelled()) return;
            GuildHandler.addXpToGuild(player.getUniqueId(), 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(Utils.c("&7⛏ &6+&e1 XP &epara seu clã por minerar minérios&6!")));
        }
    }
}

