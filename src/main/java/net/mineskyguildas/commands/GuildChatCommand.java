package net.mineskyguildas.commands;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.playerevents.PlayerChatEvent;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.enums.GuildChatType;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuildChatCommand implements CommandExecutor, TabCompleter {

    private final MineSkyGuildas plugin;
    private final GuildChatType type;

    public GuildChatCommand(MineSkyGuildas plugin, GuildChatType type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player player)) {
            s.sendMessage(Utils.c("§c❌ Este comando só pode ser executado por jogadores."));
            return true;
        }

        GuildHandler handler = plugin.getGuildHandler();

        if (args.length == 0) {
            player.sendMessage(Utils.c("§c⚠️ Uso correto: §f/." + label + " <mensagem>"));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            return true;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(Utils.c("§c🚫 Você não faz parte de nenhuma guilda."));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            return true;
        }

        if (type == GuildChatType.LEADER && !GuildRoles.isLeadership(guild.getRole(player.getUniqueId()))) {
            player.sendMessage(Utils.c("&4⚠ &cVocê não pode utilizar esse chat."));
            return true;
        }

        String message = String.join(" ", args);
        PlayerChatEvent event = new PlayerChatEvent(player, guild, message, type);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return true;
        }
        switch (type) {
            case GUILD -> handler.broadcastGuildChat(player, guild, Utils.c(message), false);
            case ALLY -> handler.broadcastGuildChat(player, guild, Utils.c(message), true);
            case LEADER -> handler.broadcastLeaderChat(player, guild, Utils.c(message));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String[] args) {
        if (s instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
        return Utils.getOnlinePlayerNames();
    }
}
