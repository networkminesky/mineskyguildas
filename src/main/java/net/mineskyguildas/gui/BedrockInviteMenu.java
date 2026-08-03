package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
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

public class BedrockInviteMenu implements Listener {
    private final MineSkyGuildas plugin;

    private static final Map<UUID, Guilds> activeInvites = new ConcurrentHashMap<>();

    public BedrockInviteMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static void openMenu(Player player, Guilds guild) {
        UUID uuid = player.getUniqueId();
        activeInvites.put(uuid, guild);

        Inventory inv = Bukkit.createInventory(null, 9, "§b📩 Convite para Clã!");

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
            acceptMeta.setDisplayName(Utils.c("&a&l[✔ Aceitar Convite]"));
            acceptMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para CONFIRMAR"),
                    Utils.c("&7e entrar para o clã.")
            ));
            acceptButton.setItemMeta(acceptMeta);
        }
        inv.setItem(1, acceptButton);

        ItemStack infoBook = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(Utils.c("&b&l" + guild.getName()));
            infoMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&bTag: &3[&f" + guild.getTag() + "&3]"),
                    Utils.c("&bNível: &3" + guild.getLevel()),
                    Utils.c("&bMembros: &3" + guild.getMembers().size())
            ));
            infoMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            infoBook.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoBook);

        ItemStack rejectButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta rejectMeta = rejectButton.getItemMeta();
        if (rejectMeta != null) {
            rejectMeta.setDisplayName(Utils.c("&c&l[❌ Rejeitar Convite]"));
            rejectMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para RECUSAR"),
                    Utils.c("&7e fechar este convite.")
            ));
            rejectButton.setItemMeta(rejectMeta);
        }
        inv.setItem(7, rejectButton);

        player.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (activeInvites.containsKey(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        activeInvites.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (!activeInvites.containsKey(uuid)) return;
        e.setCancelled(true);

        int slot = e.getSlot();
        if (slot == 1) {
            player.performCommand("guilda aceitar");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            activeInvites.remove(uuid);
            player.closeInventory();
        } else if (slot == 7) {
            player.performCommand("guilda rejeitar");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            activeInvites.remove(uuid);
            player.closeInventory();
        }
    }
}