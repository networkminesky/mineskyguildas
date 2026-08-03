package net.mineskyguildas.handlers.requests;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildRequestHandler {
    private final MineSkyGuildas plugin;
    private final GuildRequestType type;
    private final Map<String, Player> player = new ConcurrentHashMap<>();
    private final Map<String, Guilds> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, Guilds> pendingNotification = new ConcurrentHashMap<>();
    private final Map<String, ScheduledTask> tasks = new ConcurrentHashMap<>();

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
        player.remove(id);
        ScheduledTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }
    }

    public void sendRequest(Guilds requester, Guilds target, Player player) {
        String id = target.getId();
        pendingRequests.put(id, requester);
        this.player.put(id, player);

        if (type == GuildRequestType.WAR) {
            notifyTargetGuild(target, requester);

            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (!pendingRequests.containsKey(target.getId())) {
                    scheduledTask.cancel();
                    return;
                }
                notifyTargetGuild(target, requester);
            }, 5L * 60 * 20L, 5L * 60 * 20L);

            tasks.put(id, task);
            return;
        }

        AtomicInteger count = new AtomicInteger(0);

        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (count.getAndIncrement() >= Config.GuildInviteDuration || !notifyTargetGuild(target, requester)) {
                removeRequest(target);
                GuildHandler.broadcastGuildMessage(requester,
                        Utils.c("&4⏳&C O pedido de " + getTypeName() + " para &4" + target.getName() + " &Cexpirou."));
                scheduledTask.cancel();
            }
        }, 1L, 20L * 60);

        tasks.put(id, task);
    }

    private boolean notifyTargetGuild(Guilds target, Guilds requester) {
        boolean notified = false;

        for (UUID uuid : target.getMembers().keySet()) {
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if (targetPlayer == null || !targetPlayer.isOnline()) continue;
            if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(target.getRole(uuid))) continue;

            notifyTargetPlayer(targetPlayer, target, requester);
            notified = true;
        }

        return notified;
    }

    private void notifyTargetPlayer(Player targetPlayer, Guilds target, Guilds requester) {
        String message = type == GuildRequestType.ALLY ? "aliança" : "encerrar rivalidade";

        targetPlayer.getScheduler().run(plugin, task -> {
            if (isBedrockPlayer(targetPlayer)) {
                if (type == GuildRequestType.WAR) {
                    if (requester.isAlly(target)) {
                        targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                                Utils.c("&4&l⚔ &cSeu clã aliado &e" + requester.getName() + " &cconvidou vocês para apoiá-los em uma guerra!\n&7Utilize para aceitar: &3/clan aceitar\n&7Utilize para recusar: &3/clan rejeitar")
                        ));
                    } else {
                        targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                                Utils.c("&4&l⚔ &cSeu clã recebeu um &e&lDESAFIO DE GUERRA&c de &e" + requester.getName() + "&c!\n&7Utilize para aceitar: &3/clan aceitar\n&7Utilize para recusar: &3/clan rejeitar")
                        ));
                    }
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
                } else {
                    targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                            Utils.c("&b📩 &3Você recebeu um pedido de " + message + " de &b" + requester.getName() + "&3!\n&7Utilize para aceitar: &3/clan aceitar\n&7Utilize para recusar: &3/clan rejeitar")
                    ));
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }

                net.mineskyguildas.gui.BedrockRequestMenu.openMenu(targetPlayer, requester, type);
            }
            else {
                if (type == GuildRequestType.WAR) {
                    if (requester.isAlly(target)) {
                        targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                                Utils.c("\n" +
                                        "&4&l⚔ &cSeu clã aliado &e" + requester.getName() + " &cconvidou vocês para apoiá-los em uma guerra!\n" +
                                        "\n" +
                                        "&a&lRECOMPENSAS & VANTAGENS:\n" +
                                        " &a✔ &7Entrem em combate ativamente como aliados honorários na guerra!\n" +
                                        " &a✔ &7Demonstre lealdade militar e fortaleça suas relações políticas no servidor!\n" +
                                        "\n" +
                                        "&c&lRISCOS & REQUISITOS:\n" +
                                        " &e⚠ &7Os seus terrenos também ficarão temporariamente expostos para o clã inimigo principal!\n" +
                                        " &e⚠ &7Se o clã aliado principal perder, o seu clã &f&lnão será deletado&7, mas vocês participarão ativamente de toda a ação!\n" +
                                        "\n")
                        ));
                    } else {
                        targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                                Utils.c("\n" +
                                        "&4&l⚔ &cSeu clã recebeu um &e&lDESAFIO DE GUERRA&c de &e" + requester.getName() + "&c!\n" +
                                        "\n" +
                                        "&a&lPROS (EM CASO DE VITÓRIA):\n" +
                                        " &a✔ &7Seu clã drenará e receberá todo o saldo do banco do clã inimigo!\n" +
                                        " &a✔ &7O seu clã receberá uma recompensa militar massiva de &e10.000 XP&7!\n" +
                                        "\n" +
                                        "&c&lCONTRAS (EM CASO DE DERROTA):\n" +
                                        " &e⚠ &7Durante o período da guerra, todos os seus territórios ficarão expostos a invasões!\n" +
                                        " &e⚠ &7Em caso de derrota, seu clã será &ccompletamente erradicado e deletado&7 do servidor!\n" +
                                        "\n")
                        ));
                    }
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
                } else {
                    targetPlayer.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                            Utils.c("&b📩 &3Você recebeu um pedido de " + message + " de &b" + requester.getName() + "&3!")
                    ));
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
                targetPlayer.sendMessage(buildOptions(requester));
            }
        }, null);
    }

    private Component buildOptions(Guilds requester) {
        String cleanTag = Utils.getTag(requester.getTag());

        String acceptCmd = type == GuildRequestType.WAR ? "/clan guerra aceitar " + cleanTag : "/clan aceitar";
        String rejectCmd = type == GuildRequestType.WAR ? "/clan guerra recusar " + cleanTag : "/clan rejeitar";

        Component accept = createOption("&#6aa84f[✔ Aceitar]", "&aClique para aceitar\n&e➳ Confirmar", acceptCmd);
        Component reject = createOption("&#bf4c4c[❌ Rejeitar]", "&cClique para recusar\n&e➳ Recusar", rejectCmd);

        return LegacyComponentSerializer.legacySection().deserialize(Utils.c("&7Escolha uma opção: "))
                .append(accept)
                .append(Component.text(" "))
                .append(reject);
    }

    private Component createOption(String text, String hover, String command) {
        Component optionComponent = LegacyComponentSerializer.legacySection().deserialize(Utils.c(text));

        return optionComponent
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(Utils.c(hover))))
                .clickEvent(ClickEvent.runCommand(command));
    }

    private String getTypeName() {
        if (type == GuildRequestType.WAR) return "guerra";
        return type == GuildRequestType.ALLY ? "aliança" : "paz";
    }

    public void handleLogin(Player player, Guilds guild) {
        if (guild == null || !EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(guild.getRole(player.getUniqueId())))
            return;

        String id = guild.getId();

        if (pendingRequests.containsKey(id)) {
            Guilds requester = pendingRequests.get(id);
            notifyTargetPlayer(player, guild, requester);
        }

        if (pendingNotification.containsKey(id)) {
            Guilds requester = pendingNotification.remove(id);
            sendRequest(requester, guild, player);
        }
    }

    private boolean isBedrockPlayer(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            try {
                return org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
            } catch (Throwable ignored) {}
        }
        return player.getName().startsWith("*");
    }
}