package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.gui.GuildCreateMenu;
import net.mineskyguildas.handlers.GuildHandler;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class CreateSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "criar";
    }

    @Override
    public String getDescription() {
        return "Criar uma novo clã";
    }

    @Override
    public String getUsage() {
        return "/clan criar";
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
        if (GuildHandler.hasGuild(player)) {
            sendError(player, "&c⚠ Você já está ligado a um clã. Rompa os laços antes de criar ou buscar nova aliança.");
            return;
        }

        if (MineSkyGuildas.getInstance().getTraitorHandler().isTraitor(player.getUniqueId())) {
            sendError(player, "&4⚠ &cVocê é um TRAIDOR GLOBAL e não pode criar clãs");
        }
        GuildCreateMenu.openMainMenu(player, new GuildBuilder(player.getUniqueId()));
    }
}
