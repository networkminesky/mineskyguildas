package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.playerevents.PlayerLeaveGuildEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;
import static net.mineskyguildas.utils.Utils.confirmAction;

public class LeaveSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "abandonar";
    }

    @Override
    public String getDescription() {
        return "Abandonar sua guilda atual";
    }

    @Override
    public String getUsage() {
        return "/guilda abandonar";
    }

    @Override
    public List<String> getAliases() {
        return List.of("sair");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild.getRole(player.getUniqueId()) == GuildRoles.LEADER) {
            sendError(player, "&4⚠ &cVocê é o líder da guilda. Para sair, finalize a guilda usando &f/guildas acabar");
            return;
        }

        confirmAction(player, "confirmar a saída da guilda", () -> {
            PlayerLeaveGuildEvent event = new PlayerLeaveGuildEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A saída da guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                return;
            }
            GuildHandler.broadcastGuildMessage(guild, "&c⛔ &4" + player.getName() + " &csaiu da guilda.");
            GuildHandler.removeMember(player, guild);
            player.sendMessage(Utils.c("&a✅ Você saiu da guilda."));
        });
    }
}
