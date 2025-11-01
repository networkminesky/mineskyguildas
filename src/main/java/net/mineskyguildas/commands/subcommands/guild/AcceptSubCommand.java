package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildAddAllyEvent;
import net.mineskyguildas.api.events.GuildRemoveRivalEvent;
import net.mineskyguildas.api.events.playerevents.PlayerJoinGuildEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.handlers.requests.ReagroupHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class AcceptSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "aceitar";
    }

    @Override
    public String getDescription() {
        return "Aceitar um pedido de aliança/rivalidade, convite ou reagrupar.";
    }

    @Override
    public String getUsage() {
        return "/guild aceitar";
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
        UUID playerId = player.getUniqueId();
        Guilds ownGuild = GuildHandler.getGuildByPlayer(playerId);

        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);
        GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);
        ReagroupHandler reagroupHandler = plugin.getReagroupHandler();

        boolean hasInvite = plugin.getInviteHandler().hasInvite(playerId);
        boolean hasAllyRequest = ownGuild != null && allyHandler.hasRequest(ownGuild);
        boolean hasRivalRequest = ownGuild != null && rivalHandler.hasRequest(ownGuild);
        boolean hasReagroup = reagroupHandler.hasRequest(playerId);

        if (!hasInvite && !hasAllyRequest && !hasRivalRequest && !hasReagroup) {
            sendError(player, "&c❌ Você não tem nenhum pedido pendente para aceitar.");
            return;
        }

        if (hasReagroup) {
            reagroupHandler.accept(player);
            return;
        }

        if (hasAllyRequest) {
            Guilds allyGuild = allyHandler.getRequestGuild(ownGuild);
            GuildAddAllyEvent event = new GuildAddAllyEvent(allyGuild, ownGuild, allyHandler.getRequester(ownGuild), player);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage(event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A aliança foi interrompida pela API.") : event.CancelledMessage);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            player.sendMessage(Utils.c("&2🤝 &aVocê aceitou o pedido de aliança da guilda &2" + allyGuild.getName() + "&a!"));
            GuildHandler.addAlly(allyGuild, ownGuild, player);
            allyHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            return;
        }

        if (hasRivalRequest) {
            Guilds rivalGuild = rivalHandler.getRequestGuild(ownGuild);
            GuildRemoveRivalEvent event = new GuildRemoveRivalEvent(rivalGuild, ownGuild, rivalHandler.getRequester(ownGuild), player);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage(event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A remoção de rivalidade foi interrompida pela API.") : event.CancelledMessage);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            player.sendMessage(Utils.c("&2🕊 &aVocê aceitou o pedido de paz da guilda &2" + rivalGuild.getName() + "&a!"));
            GuildHandler.removeRival(rivalGuild, ownGuild, player);
            rivalHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            return;
        }

        if (hasInvite) {
            if (GuildHandler.hasGuild(player)) {
                sendError(player, "&4⚠ &CVocê já faz parte de uma guilda. Saia dela antes de aceitar outro convite.");
                return;
            }

            Guilds guild = plugin.getInviteHandler().getInviteGuild(playerId);
            PlayerJoinGuildEvent event = new PlayerJoinGuildEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage(event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            GuildHandler.addMember(player, guild);
            String msg = "&3🏰 &b" + player.getName() + " &3entrou na guilda &b" + guild.getName() + "&3!";
            Bukkit.broadcastMessage(Utils.c(msg));
            GuildHandler.addNotice(guild, Utils.c(msg));
            player.sendMessage(Utils.c("&a✅ Você entrou na guilda &2" + guild.getName() + "&a com sucesso!"));
            plugin.getInviteHandler().removeInvite(playerId);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }
    }
}
