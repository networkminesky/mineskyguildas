package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class TrustSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "confiar";
    }

    @Override
    public String getDescription() {
        return "Confiar um membro do clã";
    }

    @Override
    public String getUsage() {
        return "/clan confiar <player>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("trust");
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
        GuildRoles playerRole = guild.getRole(player.getUniqueId());

        if (!GuildRoles.isLeadership(playerRole)) {
            sendError(player, "&4⚠ &cVocê não tem permissão para promover membros.");
            return;
        }

        if (args.length < 2) {
            sendError(player, "&4⚠ &cUso incorreto! &7" + getUsage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetUUID = target.getUniqueId();

        if (targetUUID.equals(player.getUniqueId())) {
            sendError(player, "&4⚠ &cVocê não pode confiar a si mesmo.");
            return;
        }

        if (!GuildHandler.hasGuild(target.getPlayer()) || !GuildHandler.getGuildByPlayer(targetUUID).equals(guild)) {
            sendError(player, "&4⚠ &cEste jogador não é membro do seu clã.");
            return;
        }

        GuildRoles targetRole = guild.getRole(targetUUID);

        if (!targetRole.equals(GuildRoles.RECRUIT)) {
            sendError(player, "&4⚠ &cEsse jogador já é confiado!");
            return;
        }

        guild.getMemberData(targetUUID).setRole(GuildRoles.MEMBER);
        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " confiou no " + target.getName() + " do clã " + guild.getName());

        GuildHandler.broadcastGuildMessage(guild,
                "&3➕ &b" + target.getName() + " &3foi confiado e agora possui o cargo de &bMembro &3por &b" + player.getName() + "&3!");
    }
}
