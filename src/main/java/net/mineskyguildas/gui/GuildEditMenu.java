package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildCreateEvent;
import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.Vault;
import net.mineskyguildas.utils.ChatInputCallback;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GuildEditMenu implements Listener {
    private final MineSkyGuildas plugin;
    public static HashMap<Player, Inventory> inventories = new HashMap<>();


    public GuildEditMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static ItemStack simpleButton(Material m, String name, String... lore) {
        return simpleButton(m, name, 1, lore);
    }
    public static ItemStack simpleButton(Material m, String name, int count, String... lore) {
        ItemStack it = new ItemStack(m, count);
        ItemMeta im = it.getItemMeta();

        im.setDisplayName("§6§l"+name);

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

    private static void reorganizeItems(Inventory inv, Player player, Guilds g) {
        inv.setItem(12, simpleButton(
                Material.MAGMA_CREAM, "Tag", "• Define a tag da sua guilda",
                " ",
                "&6Tag: &e"+( g.getTag() == null || g.getTag().isEmpty() ? "Sem Tag" : g.getTag()),
                " ",
                "&e➳ Clique esquerdo - Alterar tag")
        );

        inv.setItem(14, simpleButton(
                Material.BOOK, "Descrição", "• Altere a descrição",
                " da sua guilda",
                " ",
                "&6Descrição: &e"+( g.getDescription() == null || g.getDescription().isEmpty() ? "Sem descrição" : g.getDescription()),
                " ",
                "&e➳ Clique esquerdo - Alterar descrição",
                "&e➳ Clique direito - Remover descrição")
        );
    }

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Configuração da Guilda.");

        inventories.put(player, inv);

        Guilds guilds = GuildHandler.getGuildByPlayer(player);
        reorganizeItems(inv, player, guilds);

        player.openInventory(inv);
    }

    public static void reopenInventory(Player player) {
        Inventory inv = inventories.get(player);
        Guilds guilds = GuildHandler.getGuildByPlayer(player);
        if(inv == null)
            return;

        reorganizeItems(inv, player, guilds);

        player.closeInventory();
        player.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if(inventories.containsValue(e.getInventory()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final int slot = e.getSlot();
        final ClickType clickType = e.getClick();
        Guilds g = GuildHandler.getGuildByPlayer(p);

        if(!inventories.containsValue(e.getInventory()))
            return;

        e.setCancelled(true);

        switch(slot) {
            case 12 -> {
                switch(clickType) {
                    case RIGHT -> {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                        reopenInventory(p);
                        return;
                    }
                    case LEFT -> Utils.awaitChatInput(p, new ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            if (!Utils.isValidTag(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c⚠ A tag precisa ter até " + Config.GuildTagLimit + " caracteres ou utilizou uma cor proibida. Tente uma mais curta!"));
                                return;
                            }
                            if (GuildHandler.doesGuildTagExist(response, g.getId())) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c❌ Ops! Já existe uma guilda com essa Tag. Tente outra tag!"));
                                reopenInventory(p);
                                return;
                            }
                            g.setTag(response);
                            GuildHandler.saveGuildas();
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);
                            reopenInventory(p);
                        }

                        @Override
                        public void onCancel() {
                            reopenInventory(p);
                        }
                    });
                }
            }

            case 14 -> {
                switch(clickType) {
                    case RIGHT -> {
                        g.setDescription(null);
                        GuildHandler.saveGuildas();
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);
                        reopenInventory(p);
                        return;
                    }
                    case LEFT -> Utils.awaitChatInput(p, new ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            g.setDescription(response);
                            GuildHandler.saveGuildas();
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                            reopenInventory(p);
                        }

                        @Override
                        public void onCancel() {
                            reopenInventory(p);
                        }
                    });
                }
            }
        }

        switch(clickType) {
            case RIGHT -> {
                p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 0);
                reopenInventory(p);
            }
            case LEFT -> p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);
        }

    }
}
