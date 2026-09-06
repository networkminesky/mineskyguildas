package net.mineskyguildas.commands.subcommands.guild.admin;

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

public class PromoteAdminSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "promover-admin";
    }

    @Override
    public String getDescription() {
        return "Promove o cargo de um membro de qualquer clã (somente admin).";
    }

    @Override
    public String getUsage() {
        return "/clan admin promover-admin <jogador> <cargo>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("promote-admin", "promoverglobal");
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
        UUID targetUUID = target.getUniqueId();

        Guilds guild = GuildHandler.getGuildByPlayer(targetUUID);

        if (guild == null) {
            sendError(player, "&4⚠ &cEste jogador não pertence a nenhum clã.");
            return;
        }

        GuildRoles newRole = GuildRoles.getRole(args[2]);

        if (newRole == null) {
            sendError(player,
                    "&4⚠ &cCargo inválido. Use: Líder, Sub-Líder, Capitão, Recrutador, Leal, Membro ou Recruta."
            );
            return;
        }

        guild.getMemberData(targetUUID).setRole(newRole);

        String targetName = target.getName() != null
                ? target.getName()
                : args[1];

        String newRoleName = GuildRoles.getLabelRole(newRole);
        String guildName = guild.getName();

        MineSkyGuildas.l.info(
                "[Clãs] " + player.getName()
                        + " promoveu o " + targetName
                        + " para o cargo " + newRoleName
                        + " no clã " + guildName
        );

        GuildHandler.broadcastGuildMessage(
                guild,
                "&3➕ &b" + targetName
                        + " &3foi promovido a &b" + newRoleName
                        + "&3!"
        );

        player.sendMessage(
                "§aVocê promoveu §b" + targetName
                        + " §ado clã §b" + guildName
                        + " §apara §b" + newRoleName + "§a!"
        );

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(
                    "§aVocê foi promovido a §b" + newRoleName
                            + " §apelo administrador §b"
                            + player.getName() + "§a!"
            );
        }
    }
}