package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildRemoveAllyEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class AllySubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "aliado";
    }

    @Override
    public String getDescription() {
        return "Gerenciar alianças";
    }

    @Override
    public String getUsage() {
        return "/guilda aliado <adicionar/remover/list> <guilda>";
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
            sendError(player, "&c❗ Uso correto: &f/guilda aliado <adicionar/remover> <guilda>");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&c🚫 Você não faz parte de nenhuma guilda.");
            return;
        }

        GuildRoles role = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(role) &&
                !args[1].equalsIgnoreCase("listar") && !args[1].equalsIgnoreCase("list")) {
            sendError(player, "&c🔒 Você não tem permissão para gerenciar alianças.");
            return;
        }

        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);

        switch (args[1].toLowerCase()) {
            case "adicionar", "add" -> {
                if (args.length < 3) {
                    sendError(player, "&c❗ Uso correto: &f/guilda aliado adicionar <guilda>");
                    return;
                }

                Guilds guildaTarget = GuildHandler.getGuildByTag(args[2]);
                if (guildaTarget == null) {
                    sendError(player, "&c❌ Essa guilda não existe.");
                    return;
                }

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤦 &cVocê não pode formar aliança com sua própria guilda.");
                    return;
                }

                if (guild.isRival(guildaTarget)) {
                    sendError(player, "&4⚔ &cNão é possível formar aliança com &f" + guildaTarget.getName() + " &cpois são rivais.");
                    return;
                }

                if (guild.isAlly(guildaTarget)) {
                    sendError(player, "&4🤝 &cSua guilda já é aliada da &f" + guildaTarget.getName() + ".");
                    return;
                }

                if (allyHandler.hasRequest(guildaTarget) &&
                        allyHandler.getRequestGuild(guildaTarget).getId().equals(guild.getId())) {
                    sendError(player, "&4📨 &cO pedido de aliança já foi enviado para essa guilda.");
                    return;
                }

                GuildHandler.broadcastGuildMessage(guild, Utils.c("&3📨 &b" + player.getName() + " &3enviou um pedido de aliança para &f" + guildaTarget.getName() + "&3."));
                allyHandler.sendRequest(guild, guildaTarget, player);
            }
            case "remover", "remove" -> {
                if (args.length < 3) {
                    sendError(player, "&c❗ Uso correto: &f/guilda aliado remover <guilda>");
                    return;
                }

                Guilds guildaTarget = GuildHandler.getGuildByTag(args[2]);
                if (guildaTarget == null) {
                    sendError(player, "&c❌ Essa guilda não existe.");
                    return;
                }

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤔 &cVocê não pode remover a aliança com sua própria guilda.");
                    return;
                }

                if (!guild.isAlly(guildaTarget)) {
                    sendError(player, "&c❌ Sua guilda não possui aliança com &f" + guildaTarget.getName() + "&c.");
                    return;
                }

                GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(guild, guildaTarget, player);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                GuildHandler.removeAlly(guild, guildaTarget, player);
            }
            case "listar", "list", "lista" -> {
                Guilds targetGuild = guild;

                if (args.length >= 3) {
                    targetGuild = GuildHandler.getGuildByTag(args[2]);
                    if (targetGuild == null) {
                        sendError(player, "&c❌ A guilda &f" + args[2] + " &cnão existe.");
                        return;
                    }
                }

                List<String> allyIds = targetGuild.getAllies();
                List<Guilds> allies = allyIds.stream()
                        .map(GuildHandler::getGuildByID)
                        .filter(g -> g != null)
                        .toList();

                player.sendMessage(Utils.c("&8&m----------------------------------------"));
                player.sendMessage(Utils.c("&b🤝 Alianças da guilda &f" + targetGuild.getName() + "&b:"));
                player.sendMessage(Utils.c("&7Total: &f" + allies.size()));

                if (allies.isEmpty()) {
                    player.sendMessage(Utils.c("&7❌ Nenhuma aliança ativa."));
                } else {
                    for (Guilds ally : allies) {
                        player.sendMessage(Utils.c(" &7• &f" + ally.getName() + " &8(" + ally.getTag() + "&8)"));
                    }
                }

                player.sendMessage(Utils.c("&8&m----------------------------------------"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.2f);
            }
            default -> {
                sendError(player, "&c❗ Subcomando inválido. Use: &f/guilda adicionar &cou &f/guilda remover");
            }
        }
    }
}
