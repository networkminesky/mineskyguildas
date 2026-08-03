package net.mineskyguildas.handlers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
                GuildHandler.broadcastGuildMessage(guild, Utils.c("&4⏳ &CO convite para &4" + invited.getName() + " &Cexpirou."));
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

        if (isBedrockPlayer(invited)) {
            invited.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                    Utils.c("&b\uD83D\uDCE9 &3Você recebeu um convite para o clã &b" + guildName + "&3!\n&7Utilize para aceitar: &3/clan aceitar\n&7Utilize para recusar: &3/clan rejeitar")
            ));
            invited.playSound(invited.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

            net.mineskyguildas.gui.BedrockInviteMenu.openMenu(invited, guild);
        } else {
            invited.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                    Utils.c("&b\uD83D\uDCE9 &3Você recebeu um convite para o clã &b" + guildName + "&3!")
            ));

            Component accept = createOption(
                    "&#6aa84f[✔ Aceitar convite]",
                    "&aClique para ingressar no clã\n\n&e➳ Entrar agora",
                    "/guilda aceitar"
            );

            Component reject = createOption(
                    "&#bf4c4c[❌ Rejeitar convite]",
                    "&cClique para recusar o convite do clã\n\n&e➳ Recusar agora",
                    "/guilda rejeitar"
            );

            Component options = LegacyComponentSerializer.legacySection().deserialize(Utils.c("&7Escolha uma opção: "))
                    .append(accept)
                    .append(Component.text(" "))
                    .append(reject);

            invited.sendMessage(options);
            invited.playSound(invited.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    private Component createOption(String text, String hoverText, String command) {
        Component optionComponent = LegacyComponentSerializer.legacySection().deserialize(Utils.c(text));

        return optionComponent
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(Utils.c(hoverText))))
                .clickEvent(ClickEvent.runCommand(command));
    }

    private boolean isBedrockPlayer(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            try {
                return org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
            } catch (Throwable ignored) {}
        }
        return player.getName().startsWith(".") || player.getName().startsWith("*");
    }
}