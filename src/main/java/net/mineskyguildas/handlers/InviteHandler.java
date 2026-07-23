package net.mineskyguildas.handlers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InviteHandler {
    private final MineSkyGuildas plugin;
    private final Map<UUID, Guilds> activeInvites = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> inviteTasks = new ConcurrentHashMap<>();

    public InviteHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public boolean hasInvite(UUID playerId) {
        return activeInvites.containsKey(playerId);
    }

    public Guilds getInviteGuild(UUID playerId) {
        return activeInvites.get(playerId);
    }

    public void removeInvite(UUID playerId) {
        activeInvites.remove(playerId);
        ScheduledTask task = inviteTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void sendInvite(Player invited, Guilds guild) {
        removeInvite(invited.getUniqueId());

        activeInvites.put(invited.getUniqueId(), guild);

        AtomicInteger minutes = new AtomicInteger(0);

        ScheduledTask task = invited.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!invited.isOnline() || minutes.get() >= Config.GuildInviteDuration) {
                GuildHandler.broadcastGuildMessage(guild, Utils.c("&4⏳&CO convite para &4" + invited.getName() + " &Cexpirou."));
                removeInvite(invited.getUniqueId());
                scheduledTask.cancel();
                return;
            }

            sendInviteMessage(invited, guild);
            minutes.incrementAndGet();
        }, null, 1L, 20L * 60);

        inviteTasks.put(invited.getUniqueId(), task);
    }

    private void sendInviteMessage(Player invited, Guilds guild) {
        String guildName = guild.getName();

        invited.spigot().sendMessage(new TextComponent(Utils.c("&b\uD83D\uDCE9 &3Você recebeu um convite para o clã &b" + guildName + "&3!")));

        TextComponent accept = createOption(
                "&#6aa84f[✔ Aceitar convite]",
                "§aClique para ingressar no clã\n\n§e➳ Entrar agora",
                "/guilda aceitar"
        );

        TextComponent reject = createOption(
                "&#bf4c4c[❌ Rejeitar convite]",
                "§cClique para recusar o convite do clã\n\n§e➳ Recusar agora",
                "/guilda rejeitar"
        );

        TextComponent options = new TextComponent(Utils.c("&7Escolha uma opção: "));
        options.addExtra(accept);
        options.addExtra(new TextComponent(" "));
        options.addExtra(reject);

        invited.spigot().sendMessage(options);
        invited.playSound(invited.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }

    private TextComponent createOption(String text, String hoverText, String command) {
        TextComponent component = new TextComponent(ChatColor.stripColor(Utils.c(text)));
        if (text.contains("&#")) {
            Matcher matcher = Pattern.compile("&#[A-Fa-f0-9]{6}").matcher(text);
            if (matcher.find()) {
                String hex = matcher.group().replace("&#", "#");
                component.setColor(ChatColor.of(hex));
            }
        } else {
            component.setColor(ChatColor.getByChar(text.charAt(1))); // Ex: &a
        }

        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.c(hoverText)).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }
}