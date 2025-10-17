package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Recarregar o plugin. (Não Recomendado)";
    }

    @Override
    public String getUsage() {
        return "/guilda admin reload";
    }

    @Override
    public List<String> getAliases() {
        return List.of("reconnect");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        plugin.loadConfigs();
        plugin.loadMongoDB();
        plugin.handler = new GuildHandler();
        player.sendMessage(Utils.c("&2✅ &aPlugin recarregado."));
    }
}
