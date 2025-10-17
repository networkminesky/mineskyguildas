package net.mineskyguildas.handlers.requests;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildRequestHandler {
    private final MineSkyGuildas plugin;
    private final GuildRequestType type;
    private final Map<String, Player> player = new HashMap<>();
    private final Map<String, Guilds> pendingRequests = new HashMap<>();
    private final Map<String, Guilds> pendingNotification = new HashMap<>();
    private final Map<String, BukkitRunnable> tasks = new HashMap<>();

    public GuildRequestHandler(MineSkyGuildas plugin, GuildRequestType type) {
        this.plugin = plugin;
        this.type = type;
    }

    public boolean hasRequest(Guilds target) {
        return target != null && pendingRequests.containsKey(target.getId());
    }

    public Guilds getRequestGuild(Guilds target) {
        return pendingRequests.get(target.getId());
    }

    public Player getRequester(Guilds target) {
        return player.get(target.getId());
    }

    public void removeRequest(Guilds target) {
        String id = target.getId();
        pendingRequests.remove(id);
        pendingNotification.remove(id);
        Optional.ofNullable(tasks.remove(id)).ifPresent(BukkitRunnable::cancel);
    }

    public void sendRequest(Guilds requester, Guilds target, Player player) {
        String id = target.getId();
        pendingRequests.put(id, requester);
        this.player.put(id, player);

        BukkitRunnable task = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count++ >= Config.GuildInviteDuration || !notifyTargetGuild(target, requester)) {
                    removeRequest(target);
                    GuildHandler.broadcastGuildMessage(requester,
                            Utils.c("&4⏳&C O pedido de " + getTypeName() + " para &4" + target.getName() + " &Cexpirou."));
                    cancel();
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 20L * 60);
        tasks.put(id, task);
    }

    private boolean notifyTargetGuild(Guilds target, Guilds requester) {
        String message = type == GuildRequestType.ALLY ? "aliança" : "encerrar rivalidade";
        boolean notified = false;

        for (UUID uuid : target.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(target.getRole(uuid))) continue;

            player.spigot().sendMessage(new TextComponent(Utils.c("&b📩 &3Você recebeu um pedido de " + message + " de &b" + requester.getName() + "&3!")));
            player.spigot().sendMessage(buildOptions());
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            notified = true;
        }

        return notified;
    }

    private TextComponent buildOptions() {
        TextComponent accept = createOption("&#6aa84f[✔ Aceitar]", "§aClique para aceitar\n§e➳ Confirmar", "/guilda aceitar");
        TextComponent reject = createOption("&#bf4c4c[❌ Rejeitar]", "§cClique para recusar\n§e➳ Recusar", "/guilda rejeitar");

        TextComponent options = new TextComponent(Utils.c("&7Escolha uma opção: "));
        options.addExtra(accept);
        options.addExtra(new TextComponent(" "));
        options.addExtra(reject);
        return options;
    }

    private TextComponent createOption(String text, String hover, String command) {
        TextComponent component = new TextComponent(ChatColor.stripColor(Utils.c(text)));
        Matcher matcher = Pattern.compile("&#[A-Fa-f0-9]{6}").matcher(text);
        if (matcher.find()) component.setColor(ChatColor.of(matcher.group().replace("&#", "#")));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.c(hover)).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }

    private String getTypeName() {
        return type == GuildRequestType.ALLY ? "aliança" : "paz";
    }

    public void handleLogin(Player player, Guilds guild) {
        if (guild == null || !EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(guild.getRole(player.getUniqueId())))
            return;

        String id = guild.getId();
        if (pendingNotification.containsKey(id)) {
            Guilds requester = pendingNotification.remove(id);
            sendRequest(requester, guild, player);
        }
    }
}