package net.mineskyguildas.commands.subcommands.guild;

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

public class DemoteSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "rebaixar";
    }

    @Override
    public String getDescription() {
        return "Rebaixa o cargo de um membro da guilda.";
    }

    @Override
    public String getUsage() {
        return "/guilda rebaixar <membro>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("demote");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        GuildRoles playerRole = guild.getRole(player.getUniqueId());

        if (!GuildRoles.isLeadership(playerRole)) {
            sendError(player, "&4⚠ &cVocê não tem permissão para rebaixar membros.");
            return;
        }

        if (args.length < 2) {
            sendError(player, "&4⚠ &cUso incorreto! &7" + getUsage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetUUID = target.getUniqueId();

        if (targetUUID.equals(player.getUniqueId())) {
            sendError(player, "&4⚠ &cVocê não pode rebaixar a si mesmo.");
            return;
        }

        if (!GuildHandler.hasGuild(target.getPlayer()) || !GuildHandler.getGuildByPlayer(targetUUID).equals(guild)) {
            sendError(player, "&4⚠ &cEste jogador não é membro da sua guilda.");
            return;
        }

        GuildRoles targetRole = guild.getRole(targetUUID);

        if (targetRole == GuildRoles.LEADER) {
            sendError(player, "&4⚠ &cVocê não pode rebaixar o líder da guilda!");
            return;
        }

        if (targetRole == GuildRoles.RECRUIT) {
            sendError(player, "&4⚠ &cVocê não pode rebaixar alguém que já esta com o último cargo!");
            return;
        }

        if (targetRole.ordinal() <= playerRole.ordinal()) {
            sendError(player, "&4⚠ &cVocê não pode rebaixar alguém com cargo igual ou superior ao seu!");
            return;
        }

        GuildRoles[] allRoles = GuildRoles.values();
        int newIndex = Math.min(targetRole.ordinal() + 1, allRoles.length - 1);
        GuildRoles newRole = allRoles[newIndex];

        guild.getMemberData(targetUUID).setRole(newRole);

        String newRoleName = GuildRoles.getLabelRole(newRole);

        GuildHandler.broadcastGuildMessage(guild, "&c➖ &4" + target.getName() + " &cfoi rebaixado a &4" + newRoleName + " &cpor &4" + player.getName() + "&c!");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage("§cVocê foi rebaixado a " + newRoleName + "!");
        }
    }
}