package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class FriendlyFireSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "fogo-amigo";
    }

    @Override
    public String getDescription() {
        return "Ativar/Desativar o fogo amigo da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda fogo-amigo [ativar/desativar]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ff");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds g = GuildHandler.getGuildByPlayer(player);
        if (g == null || !GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a uma guilda.");
            return;
        }
        if (!GuildRoles.isLeadershipAndRecruiter(g.getRole(player.getUniqueId()))) {
            sendError(player, "&4⚠ &cApenas os &lCAPITÕES&r &cda guilda pode alterar o status do fogo-amigo.");
            return;
        }
        if (args.length == 1) {
            if (!g.getFriendlyFire()) EnableFriendlyFire(g, player);
            else DisableFriendlyFire(g, player);
            return;
        }

        switch (args[1]) {
            case "ativar", "on", "enable", "habilitar":
                EnableFriendlyFire(g, player);
                return;
            case "desativar", "off", "disable", "desabilitar":
                DisableFriendlyFire(g, player);
                return;
        }
    }

    private void EnableFriendlyFire(Guilds g, Player player) {
        g.setFriendlyFire(true);
        GuildHandler.broadcastGuildMessage(g, "&3⚔ &b" + player.getName() + " &3habilitou o fogo-amigo.");
    }

    private void DisableFriendlyFire(Guilds g, Player player) {
        g.setFriendlyFire(false);
        GuildHandler.broadcastGuildMessage(g, "&3⚔ &b" + player.getName() + " &3desabilitou o fogo-amigo.");
    }
}
