package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.gui.GuildCreateMenu;
import net.mineskyguildas.gui.GuildEditMenu;
import net.mineskyguildas.handlers.GuildHandler;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class EditSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "editar";
    }

    @Override
    public String getDescription() {
        return "Alterar informações da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda editar [nome,tag,descrição]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("edit");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&c⚠ Você não pertence a uma guilda.");
            return;
        }

        Guilds g = GuildHandler.getGuildByPlayer(player);

        if (!GuildRoles.isLeaders(g.getRole(player.getUniqueId()))) {
            sendError(player, "&4⚠ &cApenas os &lLÍDERES&r &cda guilda pode sacar dinheiro do banco.");
            return;
        }
        GuildEditMenu.openMainMenu(player);
    }
}
