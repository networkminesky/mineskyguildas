package net.mineskyguildas.commands.subcommands.guild;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class BaseSubCommand extends SubCommand implements Listener {

    private static final Map<UUID, Location> teleportingPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, ScheduledTask> teleportTasks = new ConcurrentHashMap<>();

    public BaseSubCommand() {
        Bukkit.getPluginManager().registerEvents(this, MineSkyGuildas.getInstance());
    }

    @Override
    public String getName() {
        return "base";
    }

    @Override
    public String getDescription() {
        return "Teleportar/Setar a base do clã";
    }

    @Override
    public String getUsage() {
        return "/clan base";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds g = GuildHandler.getGuildByPlayer(player);
        if (g == null || !GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a um clã.");
            return;
        }

        if (g.getRole(player.getUniqueId()) == GuildRoles.RECRUIT) {
            player.sendMessage(Utils.c("&c❌ Você é um Recruta e possui o status de 'Não Confiável'. Você não pode se teleportar ou gerenciar a base do clã!"));
            return;
        }

        if (args.length == 1) {
            TeleportPlayerBase(player, g);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "teleportar" -> TeleportPlayerBase(player, g);
            case "setar" -> {
                if (!GuildRoles.isLeaders(g.getRole(player.getUniqueId()))) {
                    sendError(player, "&4⚠ &cApenas os &lLÍDERES&r &cdo clã podem setar a base.");
                    return;
                }
                g.setBase(player.getLocation());
                MineSkyGuildas.l.info("[Clãs] " + player.getName() + " definou a base do clã " + g.getName() + " para a localização " + player.getLocation());
                GuildHandler.broadcastGuildMessage(g, "&3🏠 &b" + player.getName() + " &3definiu a base do clã!");
            }
        }
    }

    private void TeleportPlayerBase(Player player, Guilds g) {
        if (g.getBase() == null) {
            sendError(player, "&4⚠ &cSeu clã não tem uma base definida.");
            return;
        }

        if (teleportingPlayers.containsKey(player.getUniqueId())) {
            sendError(player, "&cVocê já está aguardando o teleporte!");
            return;
        }

        Location startLocation = player.getLocation().clone();
        teleportingPlayers.put(player.getUniqueId(), startLocation);

        player.sendMessage(Utils.c("&e⏳ Fique parado por &l5 segundos&r &epara ser teleportado para a base do clã."));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        AtomicInteger seconds = new AtomicInteger(5);

        ScheduledTask task = player.getScheduler().runAtFixedRate(MineSkyGuildas.getInstance(), scheduledTask -> {
            if (!teleportingPlayers.containsKey(player.getUniqueId())) {
                scheduledTask.cancel();
                teleportTasks.remove(player.getUniqueId());
                return;
            }

            if (seconds.get() <= 0) {
                teleportingPlayers.remove(player.getUniqueId());
                teleportTasks.remove(player.getUniqueId());
                scheduledTask.cancel();

                player.teleportAsync(g.getBase(), PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(Utils.c("&a✅ Você foi teleportado para a base da seu clã!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                });
                return;
            }

            player.sendActionBar(Utils.c("&eTeleportando em &6" + seconds.get() + "s..."));
            seconds.decrementAndGet();
        }, null, 1L, 20L);

        teleportTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportingPlayers.containsKey(player.getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            teleportingPlayers.remove(player.getUniqueId());
            ScheduledTask task = teleportTasks.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
            player.sendMessage(Utils.c("&c❌ Teleporte cancelado pois você se moveu!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }
}