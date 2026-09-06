package net.mineskyguildas.commands;

import net.mineskyguildas.gui.StatsMenu;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length == 0) {
            StatsMenu.openMainMenu(player, player);
            return true;
        }

        if (args.length == 1) {

            if (!player.hasPermission("mineskyguildas.admin")) {
                player.sendMessage(Utils.c(
                        "&cVocê não tem permissão para ver as estatísticas de outros jogadores."
                ));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            StatsMenu.openMainMenu(player, target);
            return true;
        }

        player.sendMessage(Utils.c(
                "&cUtilize: /stats ou /stats <jogador>"
        ));

        return true;
    }
}