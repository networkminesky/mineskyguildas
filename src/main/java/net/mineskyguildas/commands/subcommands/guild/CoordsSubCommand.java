package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.SuperVanishHook;
import net.mineskyguildas.hooks.WorldGuardHook;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class CoordsSubCommand extends SubCommand {

    private final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public String getName() {
        return "coordenadas";
    }

    @Override
    public String getDescription() {
        return "Visualizar a localização dos membros da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda coordendas [pública/privada]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("coords");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 1) {
            Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());

            if (guild == null) {
                sendError(player, "&c❌ Você não faz parte de uma guilda.");
                return;
            }

            player.sendMessage(Utils.c("&3✨ &bCoordenadas da &3" + guild.getName()));

            Location base = player.getLocation();
            for (UUID memberId : guild.getMembers().keySet()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(memberId);
                if (!p.isOnline()) continue;

                MineSkyGuildas.getInstance().getPlayerData().getStatusCoord(p.getUniqueId(), isPublic -> {
                    if (!isPublic) return;
                    if (SuperVanishHook.isPlayerVanished(p.getPlayer())) return;

                    Player onlineMember = p.getPlayer();
                    if (onlineMember == null) return;

                    Location loc = onlineMember.getLocation();
                    String region = WorldGuardHook.getRegion(onlineMember);

                    String coords = String.format(Utils.c("&bX&8: &3%d &bY&8: &3%d &bZ&8: &3%d"),
                            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                    String distanceStr;
                    if (base.getWorld() != null && loc.getWorld() != null && base.getWorld().getUID().equals(loc.getWorld().getUID())) {
                        double distance = base.distance(loc);
                        distanceStr = df.format(distance) + " blocos";
                    } else {
                        distanceStr = "Mundos diferentes";
                    }

                    player.sendMessage(Utils.c("&7- &3" + onlineMember.getName() +
                            " &8 | &3" + coords + " &8| &3" + region + " &8| &3" + distanceStr));
                });
            }
            return;
        }

        switch (args[1]) {
            case "ativar", "on", "enable", "habilitar", "publica", "public", "pública":
                Enable(player);
                return;
            case "desativar", "off", "disable", "desabilitar", "privar", "privada", "private":
                Disable(player);
                return;
        }
    }

    private void Enable(Player player) {
        MineSkyGuildas.getInstance().getPlayerData().setStatusCoord(player.getUniqueId(), true);
        player.sendMessage(Utils.c("&2✅ &aVocê ativou a sua coordenadas!"));
    }

    private void Disable(Player player) {
        MineSkyGuildas.getInstance().getPlayerData().setStatusCoord(player.getUniqueId(), false);
        player.sendMessage(Utils.c("&4✅ &cVocê privou a sua coordenadas!"));
    }
}
