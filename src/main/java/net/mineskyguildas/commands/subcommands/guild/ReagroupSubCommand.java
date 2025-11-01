package net.mineskyguildas.commands.subcommands.guild;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class ReagroupSubCommand extends SubCommand implements Listener {

    private static final Map<UUID, Location> teleportingPlayers = new HashMap<>();

    public ReagroupSubCommand() {
        Bukkit.getPluginManager().registerEvents(this, MineSkyGuildas.getInstance());
    }

    @Override
    public String getName() {
        return "reagrupar";
    }

    @Override
    public String getDescription() {
        return "Pede para todos os membros da guilda se reagrupar até você ou na base.";
    }

    @Override
    public String getUsage() {
        return "/guild reagrupar [base]";
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
            sendError(player, "&c⚠ Você não faz parte de nenhuma guilda.");
            return;
        }

        if (!GuildRoles.isLeadership(guild.getRole(player.getUniqueId()))) {
            sendError(player, "&c⚠ Apenas líderes ou capitães podem usar este comando.");
            return;
        }

        ReagroupHandler handler = MineSkyGuildas.getInstance().getReagroupHandler();


        if (args.length > 1 && args[1].equalsIgnoreCase("base")) {
            if (guild.getBase() == null) {
                sendError(player, "&c⚠ Sua guilda não tem uma base definida.");
                return;
            }

            GuildHandler.broadcastGuildMessage(guild, "&e📍 &6" + player.getName() + " &esolicitou um reagrupamento na base da guilda!");
            for (UUID memberId : guild.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    teleportWithDelay(member, guild.getBase(), "&a✅ Você foi teleportado para a base da guilda!");
                }
            }
            return;
        }

        handler.sendReagroupRequest(player, guild);
        player.sendMessage(Utils.c("&a📍 Pedido de reagrupamento enviado para todos os membros da guilda!"));
    }

    private void teleportWithDelay(Player player, Location target, String successMessage) {
        if (teleportingPlayers.containsKey(player.getUniqueId())) {
            sendError(player, "&cVocê já está aguardando o teleporte!");
            return;
        }

        Location startLocation = player.getLocation().clone();
        teleportingPlayers.put(player.getUniqueId(), startLocation);
        player.sendTitle("§e§lReagrupar", "§7Teleportando até a base da guilda...", 5, 60, 20);
        player.sendMessage(Utils.c("&e⏳ Fique parado por &l5 segundos&r &epara ser teleportado..."));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        new BukkitRunnable() {
            int seconds = 5;

            @Override
            public void run() {
                if (!teleportingPlayers.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (seconds <= 0) {
                    teleportingPlayers.remove(player.getUniqueId());
                    player.teleport(target, PlayerTeleportEvent.TeleportCause.COMMAND);
                    player.sendMessage(Utils.c(successMessage));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    cancel();
                    return;
                }

                player.sendActionBar(Utils.c("&eTeleportando em &6" + seconds + "s..."));
                seconds--;
            }
        }.runTaskTimer(MineSkyGuildas.getInstance(), 0L, 20L);
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
            player.sendMessage(Utils.c("&c❌ Teleporte cancelado pois você se moveu!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }
}
