package net.mineskyguildas.commands.subcommands.guild.admin;

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

public class DemoteAdminSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "rebaixar-admin";
    }

    @Override
    public String getDescription() {
        return "Rebaixa o cargo de um membro de qualquer guilda (somente admin).";
    }

    @Override
    public String getUsage() {
        return "/guilda admin rebaixar-admin <jogador>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("demote-admin", "rebaixarglobal");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            sendError(player, "&4⚠ &cUso incorreto! &7" + getUsage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || target.getName() == null) {
            sendError(player, "&4⚠ &cJogador não encontrado.");
            return;
        }

        UUID targetUUID = target.getUniqueId();

        Guilds guild = GuildHandler.getGuildByPlayer(targetUUID);
        if (guild == null) {
            sendError(player, "&4⚠ &cEste jogador não pertence a nenhuma guilda.");
            return;
        }

        GuildRoles targetRole = guild.getRole(targetUUID);

        if (targetRole == GuildRoles.LEADER) {
            sendError(player, "&4⚠ &cVocê não pode rebaixar o líder da guilda!");
            return;
        }

        if (targetRole == GuildRoles.RECRUIT) {
            sendError(player, "&4⚠ &cEste jogador já está no cargo mais baixo!");
            return;
        }

        GuildRoles[] allRoles = GuildRoles.values();
        int newIndex = Math.min(targetRole.ordinal() + 1, allRoles.length - 1);
        GuildRoles newRole = allRoles[newIndex];

        guild.getMemberData(targetUUID).setRole(newRole);

        String newRoleName = GuildRoles.getLabelRole(newRole);
        String guildName = guild.getName();

        GuildHandler.broadcastGuildMessage(guild,
                "&c➖ &4" + target.getName() + " &cfoi rebaixado a &4" + newRoleName + "&c!");

        player.sendMessage("§cVocê rebaixou §4" + target.getName() + " §cda guilda §4" + guildName + " §cpara §4" + newRoleName + "§c!");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage("§cVocê foi rebaixado a §4" + newRoleName + " §cpelo administrador §4" + player.getName() + "§c!");
        }
    }
}
