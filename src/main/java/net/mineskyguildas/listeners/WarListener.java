package net.mineskyguildas.listeners;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.TraitorHandler;
import net.mineskyguildas.handlers.WarHandler;
import net.mineskyguildas.hooks.ClaimHook;
import net.mineskyguildas.hooks.HuskClaimHook;
import net.mineskyguildas.utils.Utils;
import net.mineskyguildas.war.WarSession;
import org.bukkit.Bukkit;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.Optional;
import java.util.UUID;

public class WarListener implements Listener {
    private final MineSkyGuildas plugin;
    private final WarHandler warHandler;
    private final TraitorHandler traitorManager;
    private ClaimHook claimHook;

    public WarListener(MineSkyGuildas plugin, WarHandler warHandler, TraitorHandler traitorManager) {
        this.plugin = plugin;
        this.warHandler = warHandler;
        this.traitorManager = traitorManager;
        setupClaimHook();
    }

    private void setupClaimHook() {
        if (Bukkit.getPluginManager().isPluginEnabled("HuskClaims")) {
            this.claimHook = new HuskClaimHook();
        } else {
            this.claimHook = loc -> {
                for (Guilds g : GuildHandler.getGuilds().values()) {
                    if (g.getBase() != null && g.getBase().getWorld().equals(loc.getWorld())
                            && g.getBase().distanceSquared(loc) <= 2500) {
                        return g;
                    }
                }
                return null;
            };
        }
    }

    public void setClaimHook(ClaimHook customHook) {
        this.claimHook = customHook;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFlyToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        Guilds guild = GuildHandler.getGuildByPlayer(player);
        if (guild != null && warHandler.isGuildInActiveWar(guild.getId())) {
            player.setAllowFlight(false);
            player.setFlying(false);
            event.setCancelled(true);
            player.sendMessage(Utils.c("&cVocê não pode voar enquanto seu clã estiver em guerra!"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaderDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Guilds victimGuild = GuildHandler.getGuildByPlayer(victim);

        if (victimGuild != null && victimGuild.getLeader().equals(victim.getUniqueId())) {
            Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(victimGuild.getId());
            if (sessionOpt.isPresent()) {
                WarSession session = sessionOpt.get();
                String victimId = victimGuild.getId();

                if (session.getGuild1().getId().equals(victimId)) {
                    warHandler.endWar(session, session.getGuild2(), session.getGuild1(), "O líder do clã desafiante (" + victim.getName() + ") morreu.");
                } else if (session.getGuild2().getId().equals(victimId)) {
                    warHandler.endWar(session, session.getGuild1(), session.getGuild2(), "O líder do clã desafiado (" + victim.getName() + ") morreu.");
                } else {
                    if (session.getGuild1Supporters().remove(victimId)) {
                        warHandler.saveWarToDatabase(session);
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(Utils.c("&4&l⚔ GUERRA DE CLÃS ⚔"));
                        Bukkit.broadcastMessage(Utils.c("&cO líder do clã apoiador foi derrotado!"));
                        Bukkit.broadcastMessage(Utils.c("&f" + victimGuild.getName() + " &7(Líder: " + victim.getName() + ")"));
                        Bukkit.broadcastMessage(Utils.c("&eO clã foi removido da guerra como apoiador."));
                        Bukkit.broadcastMessage(" ");
                    } else if (session.getGuild2Supporters().remove(victimId)) {
                        warHandler.saveWarToDatabase(session);
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(Utils.c("&4&l⚔ GUERRA DE CLÃS ⚔"));
                        Bukkit.broadcastMessage(Utils.c("&cO líder do clã apoiador foi derrotado!"));
                        Bukkit.broadcastMessage(Utils.c("&f" + victimGuild.getName() + " &7(Líder: " + victim.getName() + ")"));
                        Bukkit.broadcastMessage(Utils.c("&eO clã foi removido da guerra como apoiador."));
                        Bukkit.broadcastMessage(" ");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaderQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Guilds guild = GuildHandler.getGuildByPlayer(player);

        if (guild != null && guild.getLeader().equals(player.getUniqueId())) {
            if (warHandler.isGuildInActiveWar(guild.getId())) {
                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage(Utils.c("&4&l⚔ ALERTA DE GUERRA ⚔"));
                Bukkit.broadcastMessage(Utils.c("&cO líder supremo do clã &f" + guild.getName() + " &7(" + player.getName() + ") &cdesconectou-se!"));
                Bukkit.broadcastMessage(Utils.c("&e⏳ Ele possui apenas &610 minutos &epara retornar ao servidor."));
                Bukkit.broadcastMessage(Utils.c("&cSe o tempo expirar, as consequências serão aplicadas ao seu clã."));
                Bukkit.broadcastMessage(" ");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (warHandler.isClaimExposed(event.getBlock().getLocation(), session, playerGuild, claimHook)) {
                int broken = session.getBlocksBroken().getOrDefault(player.getUniqueId(), 0);
                if (broken >= 10) {
                    event.setCancelled(true);
                    player.sendMessage(Utils.c("&cVocê já atingiu o limite de 10 blocos quebrados no território inimigo!"));
                } else {
                    session.getBlocksBroken().put(player.getUniqueId(), broken + 1);
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (warHandler.isClaimExposed(event.getBlock().getLocation(), session, playerGuild, claimHook)) {
                event.setCancelled(true);
                player.sendMessage(Utils.c("&cVocê não pode colocar blocos no território inimigo durante a guerra!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (event.getClickedBlock() != null && warHandler.isClaimExposed(event.getClickedBlock().getLocation(), session, playerGuild, claimHook)) {
                org.bukkit.block.Block block = event.getClickedBlock();
                String blockType = block.getType().name();

                boolean isDoorOrTrapdoor = blockType.contains("DOOR") || blockType.contains("FENCE_GATE");
                boolean isRedstone = blockType.contains("BUTTON") || blockType.contains("LEVER")
                        || blockType.contains("PLATE") || blockType.contains("TRIPWIRE");
                boolean isContainer = block.getState() instanceof Container || blockType.contains("CHEST")
                        || blockType.contains("BARREL") || blockType.contains("SHULKER_BOX");

                if (isDoorOrTrapdoor || isRedstone || isContainer) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (event.getInventory().getLocation() != null && warHandler.isClaimExposed(event.getInventory().getLocation(), session, playerGuild, claimHook)) {
                event.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        if (attacker == null) return;
        Guilds playerGuild = GuildHandler.getGuildByPlayer(attacker);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            Entity victim = event.getEntity();
            if (victim instanceof Player) return;

            if (warHandler.isClaimExposed(victim.getLocation(), session, playerGuild, claimHook)) {
                int killed = session.getEntitiesKilled().getOrDefault(attacker.getUniqueId(), 0);
                if (killed >= 10) {
                    event.setCancelled(true);
                    attacker.sendMessage(Utils.c("&cVocê atingiu o limite de 10 entidades eliminadas no território inimigo!"));
                } else {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Guilds playerGuild = GuildHandler.getGuildByPlayer(killer);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            Entity victim = event.getEntity();
            if (victim instanceof Player) return;

            if (warHandler.isClaimExposed(victim.getLocation(), session, playerGuild, claimHook)) {
                int killed = session.getEntitiesKilled().getOrDefault(killer.getUniqueId(), 0);
                session.getEntitiesKilled().put(killer.getUniqueId(), killed + 1);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Player player = event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (warHandler.isClaimExposed(event.getTo(), session, playerGuild, claimHook)) {

                if (event.isCancelled()) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;

        Player player = event.getPlayer();
        Guilds playerGuild = GuildHandler.getGuildByPlayer(player);
        if (playerGuild == null) return;

        Optional<WarSession> sessionOpt = warHandler.getActiveWarByGuild(playerGuild.getId());
        if (sessionOpt.isPresent()) {
            WarSession session = sessionOpt.get();
            if (warHandler.isClaimExposed(event.getTo(), session, playerGuild, claimHook)) {
                if (event.isCancelled()) {
                    event.setCancelled(false);
                }
            }
        }
    }
}