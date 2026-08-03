package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BedrockReagroupMenu implements Listener {
    private final MineSkyGuildas plugin;

    private static final Map<UUID, String> activeRequests = new ConcurrentHashMap<>();

    public BedrockReagroupMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static void openMenu(Player player, String requesterName) {
        UUID uuid = player.getUniqueId();
        activeRequests.put(uuid, requesterName);

        Inventory inv = Bukkit.createInventory(null, 9, "§b📍 Reagrupamento do Clã!");
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = grayGlass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            grayGlass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 9; i++) inv.setItem(i, grayGlass);

        ItemStack acceptButton = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta acceptMeta = acceptButton.getItemMeta();
        if (acceptMeta != null) {
            acceptMeta.setDisplayName(Utils.c("&a&l[✔ Ir até o Líder]"));
            acceptMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para CONFIRMAR"),
                    Utils.c("&7e se teleportar até &b" + requesterName + "&7.")
            ));
            acceptButton.setItemMeta(acceptMeta);
        }
        inv.setItem(1, acceptButton);

        ItemStack infoBook = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(Utils.c("&e&lReagrupamento de Clã"));
            infoMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&bLíder solicitante: &f" + requesterName),
                    Utils.c("&7"),
                    Utils.c("&7O líder do seu clã deseja"),
                    Utils.c("&7reunir todos os membros!")
            ));
            infoMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            infoBook.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoBook);

        ItemStack rejectButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta rejectMeta = rejectButton.getItemMeta();
        if (rejectMeta != null) {
            rejectMeta.setDisplayName(Utils.c("&c&l[❌ Recusar Reagrupar]"));
            rejectMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para RECUSAR"),
                    Utils.c("&7o pedido de teleporte.")
            ));
            rejectButton.setItemMeta(rejectMeta);
        }
        inv.setItem(7, rejectButton);

        player.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (activeRequests.containsKey(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        activeRequests.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (!activeRequests.containsKey(uuid)) return;
        e.setCancelled(true);

        int slot = e.getSlot();
        if (slot == 1) {
            player.performCommand("guild aceitar");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            activeRequests.remove(uuid);
            player.closeInventory();
        } else if (slot == 7) {
            player.performCommand("guild rejeitar");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            activeRequests.remove(uuid);
            player.closeInventory();
        }
    }
}