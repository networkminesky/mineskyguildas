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

public class PromoteSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "promover";
    }

    @Override
    public String getDescription() {
        return "Promove o cargo de um membro do clã.";
    }

    @Override
    public String getUsage() {
        return "/clan promover <membro> <cargo>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("promote");
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

        if (args.length < 3) {
            sendError(player, "&4⚠ &cUso incorreto! &7" + getUsage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetUUID = target.getUniqueId();

        // Verifica se o jogador existe
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sendError(player, "&4⚠ &cJogador não encontrado.");
            return;
        }

        // Não pode promover a si mesmo
        if (targetUUID.equals(player.getUniqueId())) {
            sendError(player, "&4⚠ &cVocê não pode promover a si mesmo.");
            return;
        }

        // Verifica a guilda pelo UUID, funcionando offline
        Guilds targetGuild = GuildHandler.getGuildByPlayer(targetUUID);

        if (targetGuild == null || !targetGuild.equals(guild)) {
            sendError(player, "&4⚠ &cEste jogador não é membro do seu clã.");
            return;
        }

        GuildRoles targetRole = guild.getRole(targetUUID);

        if (targetRole == null) {
            sendError(player, "&4⚠ &cNão foi possível identificar o cargo deste membro.");
            return;
        }

        GuildRoles newRole = GuildRoles.getRole(args[2]);

        if (newRole == null || newRole.equals(GuildRoles.LEADER)) {
            sendError(player,
                    "&4⚠ &cCargo inválido. Use: Sub-Líder, Capitão, Recrutador, Leal, Membro ou Recruta."
            );
            return;
        }

        if (!GuildRoles.canPromoteTo(playerRole, targetRole, newRole)) {
            sendError(player, "&4⚠ &cVocê não pode promover alguém para esse cargo!");
            return;
        }

        guild.getMemberData(targetUUID).setRole(newRole);

        String targetName = target.getName() != null
                ? target.getName()
                : args[1];

        String newRoleName = GuildRoles.getLabelRole(newRole);

        MineSkyGuildas.l.info(
                "[Clãs] " + player.getName()
                        + " promoveu o " + targetName
                        + " para " + newRole.name()
                        + " no clã " + guild.getName()
        );

        GuildHandler.broadcastGuildMessage(
                guild,
                "&3➕ &b" + targetName
                        + " &3foi promovido a &b" + newRoleName
                        + " &3por &b" + player.getName() + "&3!"
        );

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(
                    "§aVocê foi promovido a " + newRoleName + "!"
            );
        }
    }
}