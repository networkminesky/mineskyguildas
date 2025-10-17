package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class RejectSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "rejeitar";
    }

    @Override
    public String getDescription() {
        return "Rejeitar um pedido de aliança/rivalidade ou convite";
    }

    @Override
    public String getUsage() {
        return "/guilda rejeitar";
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
        UUID playerId = player.getUniqueId();
        Guilds ownGuild = GuildHandler.getGuildByPlayer(playerId);
        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);
        GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);

        boolean hasInvite = plugin.getInviteHandler().hasInvite(playerId);
        boolean hasAllyRequest = allyHandler.hasRequest(ownGuild);
        boolean hasRivalRequest = rivalHandler.hasRequest(ownGuild);

        if (!hasInvite && !hasAllyRequest && !hasRivalRequest) {
            sendError(player, "&c❌ Seu convite ou pedido de aliança/paz expirou.");
            return;
        }

        if (hasAllyRequest) {
            Guilds allyGuild = allyHandler.getRequestGuild(ownGuild);

            GuildHandler.broadcastGuildMessage(allyGuild, Utils.c("&c✘ O pedido de aliança feito para &4" + ownGuild.getName() + " &cfoi rejeitado."));
            GuildHandler.broadcastGuildMessage(ownGuild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o pedido de aliança da guilda &4" + allyGuild.getName() + "&c."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o pedido de aliança da guilda &4" + allyGuild.getName() + "&c."));

            allyHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasRivalRequest) {
            Guilds g = rivalHandler.getRequestGuild(ownGuild);

            GuildHandler.broadcastGuildMessage(g, Utils.c("&c✘ O pedido de paz feito para &4" + ownGuild.getName() + " &cfoi rejeitado."));
            GuildHandler.broadcastGuildMessage(ownGuild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o pedido de paz da guilda &4" + g.getName() + "&c."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o pedido de paz da guilda &4" + g.getName() + "&c."));

            rivalHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasInvite) {
            if (GuildHandler.hasGuild(player)) {
                sendError(player, "&4⚠ &CVocê já faz parte de uma guilda. Saia dela antes de rejeitar outro convite.");
                return;
            }

            Guilds guild = plugin.getInviteHandler().getInviteGuild(playerId);
            GuildHandler.broadcastGuildMessage(guild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o convite para entrar na guilda."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o convite da guilda &4" + guild.getName() + "&c."));

            plugin.getInviteHandler().removeInvite(playerId);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }
    }
}
