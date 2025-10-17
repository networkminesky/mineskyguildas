package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildDisbandEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;
import static net.mineskyguildas.utils.Utils.confirmAction;

public class DisbandSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "desbandar";
    }

    @Override
    public String getDescription() {
        return "Deletar sua guilda atual";
    }

    @Override
    public String getUsage() {
        return "/guilda desbandar";
    }

    @Override
    public List<String> getAliases() {
        return List.of("acabar");
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
        if (!(guild.getRole(player.getUniqueId()) == GuildRoles.LEADER)) {
            sendError(player, "&4⚠ &cApenas o &lLÍDER&r &cda guilda pode desbandar.");
            return;
        }

        confirmAction(player, "desbandar sua guilda", () -> {
            GuildDisbandEvent event = new GuildDisbandEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                return;
            }
            Bukkit.broadcastMessage(Utils.c("&4⛔ &cA guilda &f" + guild.getName() + " &cfoi desbandada."));
            GuildHandler.deleteGuild(guild.getId());
            player.sendMessage(Utils.c("&2✅ &aGuilda desbandada com sucesso."));
        });
    }
}
