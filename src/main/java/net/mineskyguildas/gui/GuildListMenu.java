package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GuildListMenu implements Listener {
    private final MineSkyGuildas plugin;
    public static HashMap<Player, Inventory> inventories = new HashMap<>();
    public static HashMap<Player, Integer> playerPages = new HashMap<>();

    public GuildListMenu(MineSkyGuildas plugin) {
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

    public static ItemStack simpleButton(ItemStack it, String name, String... lore) {
        ItemMeta im = it.getItemMeta();
        im.setDisplayName("§6§l" + Utils.c(name));
        im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        im.setLore(Arrays.stream(lore).map(a -> Utils.c("&7" + a)).collect(Collectors.toList()));
        it.setItemMeta(im);
        return it;
    }

    public static void reorganizeItems(Player player, Inventory inv, int page) {
        List<Guilds> guildsList = new ArrayList<>(GuildHandler.getGuilds().values());

        int guildsPerPage = 45;
        int maxPage = (int) Math.ceil((double) guildsList.size() / guildsPerPage);
        if (maxPage == 0) maxPage = 1;

        page = Math.max(1, Math.min(page, maxPage));
        playerPages.put(player, page);

        inv.clear();

        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }

        int start = (page - 1) * guildsPerPage;
        int end = Math.min(start + guildsPerPage, guildsList.size());

        int slot = 9;
        for (int i = start; i < end; i++) {

            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot >= 45) break;
            }

            Guilds g = guildsList.get(i);
            if (g == null) continue;
            int finalSlot = slot;
            Utils.getGuildInfoAsync(g, it -> inv.setItem(finalSlot, it));
            slot++;

            if (slot >= 45) break;
        }

        inv.setItem(45, simpleButton(Material.ARROW, "Página Anterior", "Voltar uma página."));
        inv.setItem(49, simpleButton(Material.RED_WOOL, "Voltar", "Voltar para o menu anterior."));
        inv.setItem(53, simpleButton(Material.ARROW, "Próxima Página", "Avançar uma página."));
    }

    public static void openMainMenu(Player player) {
        openMainMenu(player, 1);
    }

    public static void openMainMenu(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, Utils.c("§8Guildas — Página 1"));
        inventories.put(player, inv);
        reorganizeItems(player, inv, page);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public static void reopenInventory(Player player) {
        Inventory inv = inventories.get(player);
        if (inv == null)
            return;
        reorganizeItems(player, inv, playerPages.getOrDefault(player, 1));
        player.closeInventory();
        player.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (inventories.containsValue(e.getInventory()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final int slot = e.getSlot();
        final ClickType clickType = e.getClick();

        if (!inventories.containsValue(e.getInventory()))
            return;
        e.setCancelled(true);

        switch (slot) {
            case 45 -> {
                switch (clickType) {
                    case RIGHT -> {
                        p.closeInventory();
                        return;
                    }
                    case LEFT -> {
                        openMainMenu(p, playerPages.get(p) - 1);
                    }
                }
            }
            case 49 -> {
                switch (clickType) {
                    case RIGHT, LEFT -> {
                        GuildMenu.openMainMenu(p);
                        return;
                    }
                }
            }
            case 53 -> {
                switch (clickType) {
                    case RIGHT -> {
                        p.closeInventory();
                        return;
                    }
                    case LEFT -> {
                        openMainMenu(p, playerPages.get(p) + 1);
                    }
                }
            }
        }
    }
}
