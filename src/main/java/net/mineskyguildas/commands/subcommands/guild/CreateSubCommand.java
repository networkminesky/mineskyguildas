package net.mineskyguildas.commands.subcommands.guild;

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
        return "Criar uma nova guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda criar";
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
            sendError(player, "&c⚠ Você já está ligado a uma guilda. Rompa os laços antes de criar ou buscar nova aliança.");
            return;
        }
        GuildCreateMenu.openMainMenu(player, new GuildBuilder(player.getUniqueId()));
    }
}
