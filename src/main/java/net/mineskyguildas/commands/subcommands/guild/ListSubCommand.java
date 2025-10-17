package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class ListSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "lista";
    }

    @Override
    public String getDescription() {
        return "Lista todas as guildas existentes com nome, tag e ID";
    }

    @Override
    public String getUsage() {
        return "/guilda listar";
    }

    @Override
    public List<String> getAliases() {
        return List.of("list", "listas", "guildas");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (GuildHandler.getGuilds().isEmpty()) {
            sendError(player, "&c❌ Nenhuma guilda foi encontrada.");
            return;
        }

        player.sendMessage(Utils.c("&3✨ &bLista de todas as Guildas:"));

        int i = 1;
        for (Guilds guild : GuildHandler.getGuilds().values()) {
            String name = guild.getName();
            String tag = guild.getTag();
            String id = guild.getId();

            player.sendMessage(Utils.c(String.format(
                    "&3%d&7. &bNome: &f%s &7| &bTag: &f%s &7| &bID: &8%s",
                    i++, name, tag, id
            )));
        }

        player.sendMessage(Utils.c("&7Total de guildas: &b" + GuildHandler.getGuilds().size()));
    }

}
