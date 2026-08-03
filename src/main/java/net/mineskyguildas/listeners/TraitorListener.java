package net.mineskyguildas.listeners;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.TraitorHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class TraitorListener implements Listener {
    private final MineSkyGuildas plugin;
    private final TraitorHandler traitorManager;
    private final double XP_REWARD = 2500.0;

    public TraitorListener(MineSkyGuildas plugin, TraitorHandler traitorManager) {
        this.plugin = plugin;
        this.traitorManager = traitorManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTraitorDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && traitorManager.isTraitor(victim.getUniqueId())) {
            Guilds killerGuild = GuildHandler.getGuildByPlayer(killer);

            if (killerGuild != null) {
                if (traitorManager.canClanReceiveXP(killerGuild.getId())) {
                    killerGuild.addXP(XP_REWARD);
                    GuildHandler.saveGuildas();

                    traitorManager.applyClanCooldown(killerGuild.getId());
                    traitorManager.removeTraitor(victim.getUniqueId());

                    final Location location = killer.getLocation();
                    location.getWorld().strikeLightningEffect(location);

                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage(Utils.c("&2&lTRAIDOR MORTO!"));
                    Bukkit.broadcastMessage(Utils.c(
                            "&8[&c⚔&8] &e" + killer.getName() +
                                    " &7derrotou &c" + victim.getName() +
                                    " &7e o seu clã recebeu &b+" + XP_REWARD + "de XP&7!"
                    ));
                    Bukkit.broadcastMessage(" ");

                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
                    });
                } else {
                    long remainingMs = traitorManager.getClanCooldownRemaining(killerGuild.getId());
                    long hours = remainingMs / (60 * 60 * 1000);
                    long minutes = (remainingMs % (60 * 60 * 1000)) / (60 * 1000);

                    killer.sendMessage(Utils.c("&cSeu clã eliminou o traidor " + victim.getName()
                            + ", mas vocês estão em cooldown por mais "
                            + hours + "h e " + minutes + "m. O status de traidor dele foi preservado!"));
                }
            }
        }
    }
}