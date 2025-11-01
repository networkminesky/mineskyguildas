package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerListMenu implements Listener {

    private final MineSkyGuildas plugin;
    public static final HashMap<Player, Inventory> inventories = new HashMap<>();
    public static final HashMap<Player, Integer> playerPages = new HashMap<>();

    public PlayerListMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static ItemStack simpleButton(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Utils.c("&6&l" + name));
        im.setLore(Arrays.stream(lore).map(a -> Utils.c("&7" + a)).collect(Collectors.toList()));
        it.setItemMeta(im);
        return it;
    }

    public static ItemStack playerButton(OfflinePlayer player, String... lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(Utils.c("&e" + player.getName()));
        meta.setLore(Arrays.stream(lore).map(a -> Utils.c("&7" + a)).collect(Collectors.toList()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        skull.setItemMeta(meta);
        return skull;
    }

    public static void reorganizeItems(Player viewer, Inventory inv, int page) {
        inv.clear();

        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        inv.setItem(49, simpleButton(Material.RED_WOOL, "Voltar", "Voltar para o menu anterior."));

        viewer.sendMessage(Utils.c("&e⏳ Carregando ranking de jogadores..."));

        Bukkit.getScheduler().runTaskAsynchronously(MineSkyGuildas.getInstance(), () -> {
            List<OfflinePlayer> players = Arrays.asList(Bukkit.getOfflinePlayers());
            List<PlayerStats> statsList = new ArrayList<>();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (OfflinePlayer p : players) {
                if (p.getName() == null) continue;
                CompletableFuture<Void> future = new CompletableFuture<>();

                MineSkyGuildas.getInstance().getPlayerData().getKills(p.getUniqueId(), kills ->
                        MineSkyGuildas.getInstance().getPlayerData().getDeaths(p.getUniqueId(), deaths -> {
                            double kdr = (deaths == 0 ? kills : ((double) kills / deaths));
                            statsList.add(new PlayerStats(p, (int) kills, (int) deaths, kdr));
                            future.complete(null);
                        })
                );

                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                statsList.sort(Comparator.comparingDouble(PlayerStats::getKdr).reversed());

                Bukkit.getScheduler().runTask(MineSkyGuildas.getInstance(), () -> {
                    int playersPerPage = 45;
                    int maxPage = (int) Math.ceil((double) statsList.size() / playersPerPage);
                    if (maxPage == 0) maxPage = 1;
                    int currentPage = Math.max(1, Math.min(page, maxPage));
                    playerPages.put(viewer, currentPage);

                    int start = (page - 1) * playersPerPage;
                    int end = Math.min(start + playersPerPage, statsList.size());
                    int slot = 9;

                    for (int i = start; i < end; i++) {
                        PlayerStats ps = statsList.get(i);
                        Player player = ps.player.getPlayer();
                        if (player.hasPermission("mineskyguildas.kdr.exclude")) {
                            continue;
                        }
                        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());

                        inv.setItem(slot++, playerButton(
                                player,
                                "&6Posição: &e#" + (i + 1),
                                "&6Guilda: &e" + (guild == null ? "Sem Guilda" : guild.getName()) +
                                        (guild == null ? "" : " &6[&f" + guild.getTag() + "&6]"),
                                "&6Cargo: &e" + (guild == null ? "Nenhum" : GuildRoles.getLabelRole(guild.getRole(ps.player.getUniqueId()))),
                                "&6Kills: &e" + ps.kills,
                                "&6Mortes: &e" + ps.deaths,
                                "&6KDR: &e" + new DecimalFormat("0.00").format(ps.kdr)
                        ));
                    }

                    inv.setItem(45, simpleButton(Material.ARROW, "Página Anterior", "Voltar uma página."));
                    inv.setItem(53, simpleButton(Material.ARROW, "Próxima Página", "Avançar uma página."));

                    viewer.closeInventory();
                    viewer.openInventory(inv);
                    viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                });
            });
        });
    }

    public static void openMainMenu(Player player) {
        openMainMenu(player, 1);
    }

    public static void openMainMenu(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, Utils.c("§8Jogadores — Página " + page));
        inventories.put(player, inv);
        player.openInventory(inv);
        reorganizeItems(player, inv, page);
    }

    public static void reopenInventory(Player player) {
        Inventory inv = inventories.get(player);
        if (inv == null) return;
        reorganizeItems(player, inv, playerPages.getOrDefault(player, 1));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (inventories.containsValue(e.getInventory())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final int slot = e.getSlot();
        final ClickType clickType = e.getClick();

        if (!inventories.containsValue(e.getInventory())) return;
        e.setCancelled(true);

        switch (slot) {
            case 45 -> {
                if (clickType == ClickType.LEFT)
                    openMainMenu(p, playerPages.get(p) - 1);
                else
                    p.closeInventory();
            }
            case 49 -> GuildMenu.openMainMenu(p);
            case 53 -> {
                if (clickType == ClickType.LEFT)
                    openMainMenu(p, playerPages.get(p) + 1);
                else
                    p.closeInventory();
            }
        }
    }

    private static class PlayerStats {
        private final OfflinePlayer player;
        private final int kills;
        private final int deaths;
        private final double kdr;

        public PlayerStats(OfflinePlayer player, int kills, int deaths, double kdr) {
            this.player = player;
            this.kills = kills;
            this.deaths = deaths;
            this.kdr = kdr;
        }

        public double getKdr() {
            return kdr;
        }
    }
}
