package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        GuildRoles rolePlayer = guild.getRole(player.getUniqueId());

        if (!GuildRoles.isLeadership(rolePlayer)) {
            sendError(player, "&4⚠ &cApenas os &lCAPITÕES&r &cda guilda podem expulsar membros.");
            return;
        }

        if (args.length < 2 || args[1].isEmpty()) {
            sendError(player, "&4⚠ &cVocê deve indicar um membro para expulsar.");
            return;
        }

        String targetName = args[1];

        OfflinePlayer who = Bukkit.getPlayerExact(targetName);
        if (who == null) {
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                if (op.getName() != null && op.getName().equalsIgnoreCase(targetName)) {
                    who = op;
                    break;
                }
            }
        }

        if (who == null) {
            who = Bukkit.getOfflinePlayer(targetName);
        }

        GuildRoles roleWho = guild.getRole(who.getUniqueId());

        if (roleWho == null) {
            sendError(player, "&4⚠ &cEste jogador não pertence à sua guilda.");
            return;
        }

        if (who.getUniqueId().equals(player.getUniqueId())) {
            sendError(player, "&4⚠ &cVocê não pode expulsar a si mesmo.");
            return;
        }

        if (who.getUniqueId().equals(guild.getLeader())) {
            sendError(player, "&4⚠ &cVocê não pode expulsar o líder da guilda.");
            return;
        }

        if (!GuildRoles.canPermission(rolePlayer, roleWho)) {
            sendError(player, "&4⚠ &cVocê não pode expulsar alguém com um cargo elevado.");
            return;
        }

        GuildHandler.removeMember(who.getUniqueId(), guild);
        String name = who.getName() != null ? who.getName() : targetName;

        GuildHandler.broadcastGuildMessage(
                guild,
                "&3🚧 &b" + name + " &3foi expulso por &b" + player.getName() + "&3."
        );

        if (who.isOnline() && who.getPlayer() != null) {
            who.getPlayer().sendMessage(
                    Utils.c("&4🚧 &cVocê foi expulso da " + guild.getName())
            );
        }
    }
}