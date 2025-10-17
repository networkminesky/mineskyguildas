package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildPostNoticeEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class AnnouncerSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "anunciar";
    }

    @Override
    public String getDescription() {
        return "Fazer um anúncio para todos os membros da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda anunciar <mensagem>";
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
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (!(GuildRoles.isLeadership(guild.getRole(player.getUniqueId())))) {
            sendError(player, "&4⚠ &cApenas os &lCAPITÕES&r &cda guilda pode anunciar.");
            return;
        }

        if (args.length < 2 || args[1].isEmpty()) { // Added length check for args
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
        GuildHandler.broadcastGuildMessageNoNotice(guild, Utils.c("&b✉ &3" + player.getName() + "&8: &f" + message));
    }
}
