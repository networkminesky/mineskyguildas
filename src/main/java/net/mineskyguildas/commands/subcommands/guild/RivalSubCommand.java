package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildAddRivalEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class RivalSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "rival";
    }

    @Override
    public String getDescription() {
        return "Gerenciar rivais";
    }

    @Override
    public String getUsage() {
        return "/guilda rival <adicionar/remover> <guilda>";
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
        if (args.length < 3) {
            sendError(player, "&c❗ Uso correto: &f/guilda rival <adicionar/remover> <guilda>");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&c🚫 Você não faz parte de nenhuma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(cargo)) {
            sendError(player, "&c🔒 Você não tem permissão para declarar rivalidade.");
            return;
        }

        Guilds guildaTarget = GuildHandler.getGuildByTag(args[2]);

        if (guildaTarget == null) {
            sendError(player, "&c❌ Essa guilda não existe.");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "adicionar", "add" -> {
                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤦 &CVocê não pode criar rivalidade com sua própria guilda.");
                    return;
                }

                if (guild.isAlly(guildaTarget)) {
                    sendError(player, "&c⚠ A guilda &f" + guildaTarget.getName() + " &cé sua aliada. Remova a aliança antes de declarar rivalidade.");
                    return;
                }

                if (guild.isRival(guildaTarget)) {
                    sendError(player, "&4😠 &CSua guilda já é rival da &f" + guildaTarget.getName() + ".");
                    return;
                }

                GuildAddRivalEvent event = new GuildAddRivalEvent(guild, guildaTarget, player);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                GuildHandler.addRival(guild, guildaTarget, player);
                player.sendMessage(Utils.c("&4⚔ &cVocê declarou rivalidade com a guilda &f" + guildaTarget.getName() + "&c!"));
            }

            case "remover", "remove" -> {
                GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤔 &CVocê não pode remover rivalidade com sua própria guilda.");
                    return;
                }

                if (!guild.isRival(guildaTarget)) {
                    sendError(player, "&4📛 &cSua guilda não é rival da &f" + guildaTarget.getName() + ".");
                    return;
                }

                if (rivalHandler.hasRequest(guildaTarget) &&
                        rivalHandler.getRequestGuild(guildaTarget).getId().equals(guild.getId())) {
                    sendError(player, "&4📨 &cO pedido de paz já foi enviado para essa guilda.");
                    return;
                }

                GuildHandler.broadcastGuildMessage(guild, Utils.c("&3📨 &b" + player.getName() + " &3enviou um pedido de paz para &f" + guildaTarget.getName() + "&3."));
                rivalHandler.sendRequest(guild, guildaTarget, player);
                player.sendMessage(Utils.c("&a✔ Um pedido de paz para &f" + guildaTarget.getName() + " &afoi enviada com sucesso."));
            }

            default -> {
                sendError(player, "&c❗ Subcomando inválido. Use: &f/adicionar &cou &f/remover");
            }
        }
    }
}
