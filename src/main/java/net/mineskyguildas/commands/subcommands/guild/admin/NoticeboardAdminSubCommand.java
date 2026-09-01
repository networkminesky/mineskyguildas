package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.Notice;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class NoticeboardAdminSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "mural-admin";
    }

    @Override
    public String getDescription() {
        return "Veja todas as notificações do mural de um clã específico (Admin)";
    }

    @Override
    public String getUsage() {
        return "/clan admin mural-admin <clã>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("anuncios", "vernoticias");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            sendError(player, "&4⚠ &cUso correto: " + getUsage());
            return;
        }

        String tag = args[1];

        Guilds guild = GuildHandler.getGuildByTag(tag);

        if (guild == null) {
            sendError(player, "&4⚠ &cO clã '&e" + tag + "&c' não foi encontrado.");
            return;
        }

        List<Notice> notices = guild.getNoticeBoard();

        player.sendMessage(Utils.c("&e✉ &6Mural do clã &e" + guild.getName() + " &7(Visão Admin)&6:"));

        if (notices.isEmpty()) {
            player.sendMessage(Utils.c("&7(sem mensagens ainda)"));
            return;
        }

        for (int i = 0; i < notices.size(); i++) {
            String message = notices.get(i).getMessage();
            player.sendMessage(Utils.c("&7" + (i + 1) + ". " + message));
        }
    }
}