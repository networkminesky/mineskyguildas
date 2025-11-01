package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.SuperVanishHook;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class MembersSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "membros";
    }

    @Override
    public String getDescription() {
        return "Lista dos membros de uma guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda membros [tag]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("membro");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds guild = (args.length > 1 && args[1] != null)
                ? GuildHandler.getGuildByTag(args[1])
                : GuildHandler.getGuildByPlayer(player.getUniqueId());

        if (guild == null) {
            sendError(player, "&c❌ Essa guilda não existe.");
            return;
        }

        player.sendMessage(Utils.c("&3✨ &bMembros da &3" + guild.getName()));

        int i = 1;
        for (UUID memberId : guild.getMembers().keySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);

            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "&cDesconhecido";
            String role = GuildRoles.getLabelRole(guild.getMemberData(memberId).getRole());
            String status = offlinePlayer.isOnline() && !SuperVanishHook.isPlayerVanished(offlinePlayer.getPlayer()) ? "&aONLINE" : "&cOFFLINE";

            player.sendMessage(Utils.c(String.format("&3%d&7. &b%s &7- &3%s &8(%s&8)", i++, name, role, status)));
        }
    }
}
