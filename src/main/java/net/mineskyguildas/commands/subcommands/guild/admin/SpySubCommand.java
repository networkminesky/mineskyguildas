package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.GuildCommand;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class SpySubCommand extends SubCommand {
    @Override
    public String getName() {
        return "spy";
    }

    @Override
    public String getDescription() {
        return "Alterar o status de spy";
    }

    @Override
    public String getUsage() {
        return "/guild admin spy";
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
        if (!player.hasPermission("mineskyguildas.spy")) {
            GuildCommand.sendError(player, "&4⚠ &cVocê não tem permissão.");
            return;
        }

        if (args.length == 1) {
            MineSkyGuildas.getInstance().getPlayerData().getSpy(player.getUniqueId(), status -> {
                if (!status) EnableSpy(player);
                else DisableSpy(player);
            });
            return;
        }

        switch (args[1]) {
            case "ativar", "on", "enable", "habilitar":
                EnableSpy(player);
                return;
            case "desativar", "off", "disable", "desabilitar":
                DisableSpy(player);
                return;
        }
    }

    private void EnableSpy(Player player) {
        MineSkyGuildas.getInstance().getPlayerData().setSpy(player.getUniqueId(), true);
        player.sendMessage(Utils.c("&2✅ &aVocê ativou seu spy!"));
    }

    private void DisableSpy(Player player) {
        MineSkyGuildas.getInstance().getPlayerData().setSpy(player.getUniqueId(), false);
        player.sendMessage(Utils.c("&4✅ &cVocê desativou seu spy!"));
    }
}
