package net.mineskyguildas.handlers.requests;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReagroupHandler {

    private final MineSkyGuildas plugin;
    private final Map<UUID, UUID> active = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public ReagroupHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public boolean hasRequest(UUID playerId) {
        return active.containsKey(playerId);
    }

    public UUID getRequester(UUID playerId) {
        return active.get(playerId);
    }

    public void removeRequest(UUID playerId) {
        active.remove(playerId);
        ScheduledTask task = tasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void sendReagroupRequest(Player requester, Guilds guild) {
        for (UUID memberId : guild.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || member.equals(requester)) continue;

            sendReagroupMessage(member, requester, guild);
            active.put(memberId, requester.getUniqueId());

            AtomicInteger seconds = new AtomicInteger(0);

            ScheduledTask task = member.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (!member.isOnline() || seconds.get() >= Config.GuildInviteDuration * 60) {
                    removeRequest(memberId);
                    scheduledTask.cancel();
                    return;
                }
                seconds.incrementAndGet();
            }, null, 1L, 20L);

            tasks.put(memberId, task);
        }

        GuildHandler.broadcastGuildMessage(guild,
                Utils.c("&e📍 &fO jogador &b" + requester.getName() + " &fescolheu um local para reagrupar o clã!"));
    }

    private void sendReagroupMessage(Player member, Player requester, Guilds guild) {
        String guildName = guild.getName();

        member.getScheduler().run(plugin, scheduledTask -> {
            member.sendMessage(Utils.c("&b\uD83D\uDDFA &3O líder do clã &b" + guildName + " &3deseja reagrupar todos os membros!"));

            TextComponent accept = createOption(
                    "&#6aa84f[✔ Ir até o líder]",
                    "§aClique para se teleportar até " + requester.getName(),
                    "/guild aceitar"
            );

            TextComponent reject = createOption(
                    "&#bf4c4c[❌ Recusar]",
                    "§cClique para recusar o teleporte",
                    "/guild rejeitar"
            );

            TextComponent options = new TextComponent(Utils.c("&7Escolha uma opção: "));
            options.addExtra(accept);
            options.addExtra(new TextComponent(" "));
            options.addExtra(reject);

            member.spigot().sendMessage(options);
            member.playSound(member.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }, null);
    }

    private TextComponent createOption(String text, String hoverText, String command) {
        TextComponent component = new TextComponent(Utils.c(text));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.c(hoverText)).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }

    public void accept(Player member) {
        if (!hasRequest(member.getUniqueId())) {
            member.sendMessage(Utils.c("&c⚠ Nenhum pedido de reagrupar ativo."));
            return;
        }

        UUID requesterId = getRequester(member.getUniqueId());
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            member.sendMessage(Utils.c("&c⚠ O líder não está mais online."));
            removeRequest(member.getUniqueId());
            return;
        }

        member.teleportAsync(requester.getLocation()).thenAccept(success -> {
            if (success) {
                member.sendMessage(Utils.c("&a✅ Você foi teleportado até &b" + requester.getName() + "&a!"));
                requester.sendMessage(Utils.c("&a✨ " + member.getName() + " aceitou o reagrupar!"));
            } else {
                member.sendMessage(Utils.c("&c⚠ Falha ao realizar o teleporte."));
            }
            removeRequest(member.getUniqueId());
        });
    }

    public void reject(Player member) {
        if (!hasRequest(member.getUniqueId())) {
            member.sendMessage(Utils.c("&c⚠ Nenhum pedido de reagrupar ativo."));
            return;
        }

        UUID requesterId = getRequester(member.getUniqueId());
        Player requester = Bukkit.getPlayer(requesterId);

        member.sendMessage(Utils.c("&c❌ Você recusou o pedido de reagrupar."));
        if (requester != null) {
            requester.sendMessage(Utils.c("&c⚠ " + member.getName() + " recusou o reagrupar."));
        }
        removeRequest(member.getUniqueId());
    }
}