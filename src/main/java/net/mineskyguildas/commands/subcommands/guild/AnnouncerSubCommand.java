package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildPostNoticeEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class AnnouncerSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "anunciar";
    }

    @Override
    public String getDescription() {
        return "Fazer um anúncio para todos os membros do clã";
    }

    @Override
    public String getUsage() {
        return "/clan anunciar <mensagem>";
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
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhum clã no momento.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (!(GuildRoles.isLeadership(guild.getRole(player.getUniqueId())))) {
            sendError(player, "&4⚠ &cApenas os &lCAPITÕES&r &cdo clã pode anunciar.");
            return;
        }

        if (args.length < 2 || args[1].isEmpty()) {
            sendError(player, "&4⚠ &cVocê deve indicar uma mensagem para fazer um anúncio.");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        GuildPostNoticeEvent event = new GuildPostNoticeEvent(player, guild, message);
        MineSkyGuildas.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! O anúncio foi interrompida pela API.") : event.CancelledMessage));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
            return;
        }
        GuildHandler.addNotice(guild, message, player);
        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " anunciou \"" + message + "\" no clã " + guild.getName());
        Set<UUID> recipients = new HashSet<>();

        recipients.addAll(guild.getMembers().keySet());


        String spy = Utils.c("&3[Spy] " +
                "&8✉ &8[&f" + guild.getTag() + "&8] &7" + player.getName() + "&8: &f" + message);


        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !recipients.contains(p.getUniqueId()))
                .filter(p -> p.hasPermission("mineskyguildas.spy"))
                .forEach(p -> {
                    MineSkyGuildas.getInstance().getPlayerData().getSpy(p.getUniqueId(), spyAtivado -> {
                        if (spyAtivado) {
                            p.getScheduler().run(MineSkyGuildas.getInstance(), task -> {
                                p.sendMessage(spy);
                            }, null);
                        }
                    });
                });
        GuildHandler.broadcastGuildMessageNoNotice(guild, Utils.c("&b✉ &3" + player.getName() + "&8: &f" + message));
    }
}
