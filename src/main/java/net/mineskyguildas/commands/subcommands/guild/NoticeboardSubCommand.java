package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.Notice;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class NoticeboardSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "mural";
    }

    @Override
    public String getDescription() {
        return "Veja todas as notificações da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda mural";
    }

    @Override
    public List<String> getAliases() {
        return List.of("anuncios");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento");
            return;
        }
        List<Notice> notices = guild.getNoticeBoard();

        player.sendMessage(Utils.c("&e✉ &6Mural da Guilda &e" + guild.getName() + ":"));

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
