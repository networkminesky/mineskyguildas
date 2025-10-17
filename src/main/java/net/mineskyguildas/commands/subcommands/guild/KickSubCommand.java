package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class KickSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "expulsar";
    }

    @Override
    public String getDescription() {
        return "Expulsar um membro da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda expulsar <membro>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("kick", "kickar");
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
            sendError(player, "&4⚠ &cApenas os &lCAPITÕES&r &cda guilda pode expulsar membros.");
            return;
        }

        if (args.length < 2 || args[1].isEmpty()) {
            sendError(player, "&4⚠ &cVocê deve indicar um membro para expulsar.");
            return;
        }

        Player who = Bukkit.getPlayer(args[1]);
        if (who == null || !GuildHandler.getGuildByPlayer(who.getUniqueId()).equals(guild)) {
            sendError(player, "&4⚠ &cMembro não encontrado.");
            return;
        }

        if (who.equals(player)) {
            sendError(player, "&4⚠ &cVocê não pode expulsar sí mesmo.");
            return;
        }

        if (who.getUniqueId().equals(guild.getLeader())) {
            sendError(player, "&4⚠ &cVocê não pode expulsar o líder da guilda.");
            return;
        }

        GuildRoles rolePlayer = guild.getRole(player.getUniqueId());
        GuildRoles roleWho = guild.getRole(who.getUniqueId());

        if (!GuildRoles.canPermission(rolePlayer, roleWho)) {
            sendError(player, "&4⚠ &cVocê não pode expulsar alguém com um cargo elevado.");
            return;
        }

        GuildHandler.removeMember(who, guild);
        GuildHandler.broadcastGuildMessage(guild, "&3\uD83D\uDEA7 &b" + who.getName() + " &3expulso por &b" + player.getName() + "&3.");
    }
}
