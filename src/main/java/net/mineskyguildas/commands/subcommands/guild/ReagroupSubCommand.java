package net.mineskyguildas.commands.subcommands.guild;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.ReagroupHandler;
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

public class ReagroupSubCommand extends SubCommand implements Listener {

    private static final Map<UUID, Location> teleportingPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, ScheduledTask> teleportTasks = new ConcurrentHashMap<>();

    public ReagroupSubCommand() {
        Bukkit.getPluginManager().registerEvents(this, MineSkyGuildas.getInstance());
    }

    @Override
    public String getName() {
        return "reagrupar";
    }

    @Override
    public String getDescription() {
        return "Pede para todos os membros do clã se reagrupar até você ou na base.";
    }

    @Override
    public String getUsage() {
        return "/clan reagrupar [base]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("reagroup");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds guild = GuildHandler.getGuildByPlayer(player);

        if (guild == null) {
            sendError(player, "&c⚠ Você não faz parte de nenhum clã.");
            return;
        }

        if (!GuildRoles.isLeadership(guild.getRole(player.getUniqueId()))) {
            sendError(player, "&c⚠ Apenas líderes ou capitães podem usar este comando.");
            return;
        }

        ReagroupHandler handler = MineSkyGuildas.getInstance().getReagroupHandler();

        if (args.length > 1 && args[1].equalsIgnoreCase("base")) {
            if (guild.getBase() == null) {
                sendError(player, "&c⚠ Seu clã não tem uma base definida.");
                return;
            }

            GuildHandler.broadcastGuildMessage(guild, "&e📍 &6" + player.getName() + " &esolicitou um reagrupamento na base do clã!");
            MineSkyGuildas.l.info("[Clãs] " + player.getName() + " solicitou um reagrupamento na base para os membros do clã " + guild.getName());
            for (UUID memberId : guild.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    teleportWithDelay(member, guild.getBase(), "&a✅ Você foi teleportado para a base do clã!");
                }
            }
            return;
        }

        handler.sendReagroupRequest(player, guild);
        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " enviou um pedido de reagrupamento para os membros do clã " + guild.getName());
        player.sendMessage(Utils.c("&a📍 Pedido de reagrupamento enviado para todos os membros do clã!"));
    }

    private void teleportWithDelay(Player player, Location target, String successMessage) {
        if (teleportingPlayers.containsKey(player.getUniqueId())) {
            sendError(player, "&cVocê já está aguardando o teleporte!");
            return;
        }

        Location startLocation = player.getLocation().clone();
        teleportingPlayers.put(player.getUniqueId(), startLocation);
        player.sendTitle("§e§lReagrupar", "§7Teleportando até a base do clã...", 5, 60, 20);
        player.sendMessage(Utils.c("&e⏳ Fique parado por &l5 segundos&r &epara ser teleportado..."));
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

                player.teleportAsync(target, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(Utils.c(successMessage));
                        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " foi teleportado para " + target.toString());
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
            MineSkyGuildas.l.info("[Clãs] " + player.getName() + " se moveu e o teleporte foi cancelado.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }
}