package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class FriendlyFireAdminSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "fogo-amigo-global";
    }

    @Override
    public String getDescription() {
        return "Ativar ou desativar o fogo amigo de todas as guildas.";
    }

    @Override
    public String getUsage() {
        return "/guilda admin fogo-amigo-global [ativar/desativar]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ffg");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            sendError(player, "&c❗ Uso incorreto! Use: " + getUsage());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "ativar", "on", "enable", "habilitar" -> enableAllFriendlyFire(player);
            case "desativar", "off", "disable", "desabilitar" -> disableAllFriendlyFire(player);
            default -> sendError(player, "&c❗ Argumento inválido! Use 'ativar' ou 'desativar'.");
        }
    }

    private void enableAllFriendlyFire(Player player) {
        for (Guilds guild : GuildHandler.getGuilds().values()) {
            guild.setFriendlyFire(true);
        }

        Bukkit.broadcastMessage(Utils.c("&b⚔ &3Todas as guildas tiveram o fogo-amigo habilitado&b!"));
        player.sendMessage("§b⚔ Você habilitou o fogo-amigo em todas as guildas!");
    }

    private void disableAllFriendlyFire(Player player) {
        for (Guilds guild : GuildHandler.getGuilds().values()) {
            guild.setFriendlyFire(false);
        }

        Bukkit.broadcastMessage(Utils.c("&b⚔ &3Todas as guildas tiveram o fogo-amigo desabilitado&b!"));
        player.sendMessage("§b⚔ Você desabilitou o fogo-amigo em todas as guildas!");
    }
}