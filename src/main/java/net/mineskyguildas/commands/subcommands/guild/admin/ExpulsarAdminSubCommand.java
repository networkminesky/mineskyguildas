package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class ExpulsarAdminSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "expulsar-admin";
    }

    @Override
    public String getDescription() {
        return "Expulsar um membro de qualquer clã (Somente admins)";
    }

    @Override
    public String getUsage() {
        return "/clan admin expulsar-admin <player>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("kick-admin");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }


    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
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
            sendError(player, "&4⚠ &cEste jogador não pertence a nenhum clã.");
            return;
        }

        GuildHandler.removeMember(targetUUID, guild);
        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + player.getName() + " expulsou o " + target.getName() + " do clã " + guild.getName());
        String guildName = guild.getName();

        GuildHandler.broadcastGuildMessage(guild,
                "&3\uD83D\uDEA7 &b" + target.getName() + " &3expulso por &bum administrador&3.");
        player.sendMessage("§aVocê expulsou §b" + target.getName() + " §ado clã §b" + guildName);

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(Utils.c("&4\uD83D\uDEA7 &cVocê foi expulso por um administrador do clã " + guild.getName()));
        }
    }
}
