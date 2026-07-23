package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class ForceJoinSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "forçar-entrada";
    }

    @Override
    public String getDescription() {
        return "Força sua entrada em um clã ou de algum player.";
    }

    @Override
    public String getUsage() {
        return "/clan admin forçar-entrada <clan> [player] [cargo]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("forcarentrada", "forcejoin");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Utils.c("&cUso correto: " + getUsage()));
            return;
        }

        String tag = args[1];
        String playerArg = args.length >= 3 ? args[2] : player.getName();
        String cargoArg = args.length >= 4 ? args[3] : "RECRUTA"; // padrão agora é RECRUTA

        Guilds guild = GuildHandler.getGuildByTag(tag);
        if (guild == null) {
            player.sendMessage(Utils.c("&cO clã &f" + tag + " &cnão existe."));
            return;
        }

        Player target = Bukkit.getPlayerExact(playerArg);
        UUID targetId;

        if (target != null) {
            targetId = target.getUniqueId();
        } else {
            try {
                targetId = Bukkit.getOfflinePlayer(playerArg).getUniqueId();
            } catch (Exception e) {
                player.sendMessage(Utils.c("&cJogador não encontrado: &f" + playerArg));
                return;
            }
        }

        Guilds oldGuild = GuildHandler.getGuildByPlayer(targetId);
        if (oldGuild != null) {
            oldGuild.removeMember(targetId);
            player.sendMessage(Utils.c("&eO jogador foi removido do clã anterior &f" + oldGuild.getName() + "&e."));
        }
        GuildRoles newRole = GuildRoles.getRole(cargoArg);

        if (newRole == null || newRole.equals(GuildRoles.LEADER)) {
            sendError(player, "&4⚠ &cCargo inválido. Use: Sub-Líder, Capitão, Recrutador, Leal, Membro ou Recruta.");
            return;
        }
        String newRoleName = GuildRoles.getLabelRole(newRole);

        guild.addMember(targetId, newRole, 0);
        GuildHandler.saveGuildas();
        String msg = "&3🏰 &b" + target.getName() + " &3entrou no clã &b" + guild.getName() + "&3!";
        Bukkit.broadcastMessage(Utils.c(msg));
        GuildHandler.addNotice(guild, Utils.c(msg));
        player.sendMessage(Utils.c("&aVocê forçou a entrada de &f" + playerArg + " &ano clã &f" + guild.getName() + " &acom o cargo &f" + newRoleName + "&a."));
        if (target != null && target.isOnline()) {
            target.sendMessage(Utils.c("&aVocê foi adicionado no clã &f" + guild.getName() + " &acom o cargo &f" + newRoleName + "&a."));
        }
    }
}
