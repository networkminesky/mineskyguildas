package net.minesky.commands;

import net.minesky.MineSkyGuildas;
import net.minesky.handlers.GuildHandler;
import net.minesky.hooks.WorldGuardHook;
import net.minesky.data.Guilds;
import net.minesky.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Test implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command,  String s, String[] strings) {
        Player player = (Player) commandSender;
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());

        player.sendMessage("Guilda: " + guild.getName() + " Aliados: " + guild.getAllies().toString() + " Rivais: " + guild.getRivals().toString());

        MineSkyGuildas.getInstance().getPlayerData().getKDR(player.getUniqueId(), value -> player.sendMessage("KDR: " + value));
        Bukkit.broadcastMessage(WorldGuardHook.getRegions(Bukkit.getWorld("void2")).toString());
        if (strings[0].equalsIgnoreCase("set")) {
            Guilds guildaTarget = GuildHandler.getGuildByTag(strings[1]);
            if (guildaTarget == null) {
                player.sendMessage(Utils.c("&c❌ Essa guilda não existe."));
                return true;
            }

            //RegionsHandler rh = new RegionsHandler(MineSkyGuildas.getInstance());
           // rh.setRegion(strings[2], guildaTarget);
            player.sendMessage("SETADO !!!!");
        }
        return true;
    }
}
