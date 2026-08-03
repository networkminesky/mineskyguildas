package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.handlers.TraitorHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TraitorsSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "traidores";
    }

    @Override
    public String getDescription() {
        return "Lista todos os traidores ativos globais e de qual clã eram.";
    }

    @Override
    public String getUsage() {
        return "/clan traidores ou /clan lista-negra";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("lista-negra", "blacklist", "listanegra");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        TraitorHandler traitorManager = MineSkyGuildas.getInstance().getTraitorHandler();
        Map<UUID, String> traitors = traitorManager.getGlobalTraitors();

        if (traitors.isEmpty()) {
            player.sendMessage(Utils.c("&a✌ Não existem traidores ativos no servidor no momento."));
            return;
        }

        player.sendMessage(Utils.c("&c⚔ ======= [ LISTA NEGRA DE TRAIDORES ] ======="));
        for (Map.Entry<UUID, String> entry : traitors.entrySet()) {
            OfflinePlayer traitorPlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String traitorName = traitorPlayer.getName() != null ? traitorPlayer.getName() : "Desconhecido";
            player.sendMessage(Utils.c(" &c• &f" + traitorName + " &7(Ex-membro do clã: &f" + entry.getValue() + "&7)"));
        }
        player.sendMessage(Utils.c("&c============================================="));
    }
}