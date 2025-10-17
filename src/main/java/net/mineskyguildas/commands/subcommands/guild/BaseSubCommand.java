package net.mineskyguildas.commands.subcommands.guild;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class BaseSubCommand extends SubCommand implements Listener {

    private static final Map<UUID, Location> teleportingPlayers = new HashMap<>();

    @Override
    public String getName() {
        return "base";
    }

    @Override
    public String getDescription() {
        return "Teleportar/Setar a base da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda base";
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
            sendError(player, "&4⚠ &cVocê não pertence a uma guilda.");
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
                    sendError(player, "&4⚠ &cApenas os &lLÍDERES&r &cda guilda podem setar a base.");
                    return;
                }
                g.setBase(player.getLocation());
                GuildHandler.broadcastGuildMessage(g, "&3🏠 &b" + player.getName() + " &3definiu a base da guilda!");
            }
        }
    }

    private void TeleportPlayerBase(Player player, Guilds g) {
        if (g.getBase() == null) {
            sendError(player, "&4⚠ &cSua guilda não tem uma base definida.");
            return;
        }

        if (teleportingPlayers.containsKey(player.getUniqueId())) {
            sendError(player, "&cVocê já está aguardando o teleporte!");
            return;
        }

        Location startLocation = player.getLocation().clone();
        teleportingPlayers.put(player.getUniqueId(), startLocation);

        player.sendMessage(Utils.c("&e⏳ Fique parado por &l5 segundos&r &epara ser teleportado para a base da guilda."));
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
                    player.teleport(g.getBase(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    player.sendMessage(Utils.c("&a✅ Você foi teleportado para a base da sua guilda!"));
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

    public BaseSubCommand() {
        Bukkit.getPluginManager().registerEvents(this, MineSkyGuildas.getInstance());
    }
}
