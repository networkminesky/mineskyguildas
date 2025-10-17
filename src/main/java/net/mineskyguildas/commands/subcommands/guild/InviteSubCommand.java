package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildInviteEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.Vault;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class InviteSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "convidar";
    }

    @Override
    public String getDescription() {
        return "Convidar um jogador para sua guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda convidar <jogador>";
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
        if (args.length < 2) {
            sendError(player, "&4⚠ &cUso correto: /guilda convidar <jogador>");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&4⚠ &CVocê não faz parte de uma guilda.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sendError(player, "&4⚠ &CJogador não encontrado ou está offline.");
            return;
        }

        if (target.equals(player)) {
            sendError(player, "&4⚠ &cVocê não pode convidar a si mesmo.");
            return;
        }

        if (GuildHandler.hasGuild(target)) {
            sendError(player, "&4⚠ &cEste jogador já faz pertence a uma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER, GuildRoles.CAPTAIN, GuildRoles.RECRUITER).contains(cargo)) {
            sendError(player, "&4⚠ &cApenas membros autorizados podem convidar jogadores para a guilda.");
            return;
        }

        if (guild.getMembers().size() - 1 >= guild.getMemberLimit()) {
            sendError(player, "&4⚠ &cSua guilda atingiu o limite de membros (" + guild.getMemberLimit() + ")." + (guild.getLevel() >= 6 ? "" : " Suba o nível da guilda para expandir esse limite!"));
            return;
        }

        if (plugin.getInviteHandler().hasInvite(target.getUniqueId())
                && plugin.getInviteHandler().getInviteGuild(player.getUniqueId()).getId().equals(guild.getId())) {
            sendError(player, "&4⚠ &cEste jogador já foi convidado por sua guilda.");
            return;
        }

        if (!Vault.withdraw(player, Config.GuildInvitePrice)) {
            sendError(player, "&4⚠ &cVocê precisa de &4$" + Config.GuildInvitePrice + " &cpara enviar um convite.");
            return;
        }

        GuildInviteEvent event = new GuildInviteEvent(player, target, guild);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! O convite foi interrompida pela API.") : event.CancelledMessage));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
            Vault.deposit(player, Config.GuildInvitePrice);
            return;
        }
        plugin.getInviteHandler().sendInvite(target, guild);
        player.sendMessage(Utils.c("&4\uD83D\uDCB5 &cVocê gastou &4$" + Config.GuildInvitePrice + " &cpara convidar &4" + target.getName() + " &cpara a guilda."));
        GuildHandler.broadcastGuildMessage(guild, "&3\uD83D\uDCE9 &b" + target.getName() + " &3foi convidado para a guilda por &b" + player.getName() + "&3.");
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
    }
}
