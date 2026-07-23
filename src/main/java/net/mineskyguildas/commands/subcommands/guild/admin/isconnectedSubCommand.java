package net.mineskyguildas.commands.subcommands.guild.admin;

import net.minesky.api.database.PlayerDatabase;
import net.minesky.core.databridge.callbacks.FindOneCallback;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.database.MongoDBManager;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class isconnectedSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "isconnected";
    }

    @Override
    public String getDescription() {
        return "Verificar as conexões como MONGODB e MAINFRAME";
    }

    @Override
    public String getUsage() {
        return "/clan admin isconnected";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        String mongo = (MineSkyGuildas.getInstance().mm.isConnected() ? Utils.c("&aConectado e operando!") : Utils.c("Não conectado."));
        player.sendMessage(mongo);
        player.sendMessage("" + FindOneCallback.class.getProtectionDomain().getCodeSource().getLocation());
        player.sendMessage("" + PlayerDatabase.class.getProtectionDomain().getCodeSource().getLocation());
    }
}
