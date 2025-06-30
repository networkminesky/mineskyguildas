package net.minesky.handlers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minesky.MineSkyGuildas;
import net.minesky.config.Config;
import net.minesky.data.Guilds;
import net.minesky.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InviteHandler {
    private final MineSkyGuildas plugin;
    private final HashMap<UUID, Guilds> activeInvites = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> inviteTasks = new HashMap<>();

    public InviteHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public boolean hasInvite(UUID playerId) {
        return activeInvites != null ? activeInvites.containsKey(playerId) : false;
    }

    public Guilds getInviteGuild(UUID playerId) {
        return activeInvites.get(playerId);
    }

    public void removeInvite(UUID playerId) {
        activeInvites.remove(playerId);
        if (inviteTasks.containsKey(playerId)) {
            inviteTasks.get(playerId).cancel();
            inviteTasks.remove(playerId);
        }
    }

    public void sendInvite(Player invited, Guilds guild) {
        activeInvites.put(invited.getUniqueId(), guild);

        BukkitRunnable task = new BukkitRunnable() {
            int minutes = 0;

            @Override
            public void run() {
                if (!invited.isOnline() || minutes >= Config.GuildInviteDuration) {
                    GuildHandler.broadcastGuildMessage(guild, Utils.c("&4⏳&CO convite para &4" + invited.getName() + " &Cexpirou."));
                    removeInvite(invited.getUniqueId());
                    cancel();
                    return;
                }

                sendInviteMessage(invited, guild);
                minutes++;
            }
        };
        task.runTaskTimer(plugin, 0L, 20L * 60);
        inviteTasks.put(invited.getUniqueId(), task);
    }

    private void sendInviteMessage(Player invited, Guilds guild) {
        String guildName = guild.getName();

        invited.spigot().sendMessage(new TextComponent(Utils.c("&b\uD83D\uDCE9 &3Você recebeu um convite para a guilda &b" + guildName + "&3!")));

        TextComponent accept = createOption(
                "&#6aa84f[✔ Aceitar convite]",
                "§aClique para ingressar na guilda\n\n§e➳ Entrar agora",
                "/guilda aceitar"
        );

        TextComponent reject = createOption(
                "&#bf4c4c[❌ Rejeitar convite]",
                "§cClique para recusar o convite da guilda\n\n§e➳ Recusar agora",
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
