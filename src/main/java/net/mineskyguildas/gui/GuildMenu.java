package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.commands.GuildCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GuildMenu implements Listener {
    private final MineSkyGuildas plugin;
    public static HashMap<Player, Inventory> inventories = new HashMap<>();

    public GuildMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static ItemStack simpleButton(Material m, String name, String... lore) {
        return simpleButton(m, name, 1, lore);
    }
    public static ItemStack simpleButton(Material m, String name, int count, String... lore) {
        ItemStack it = new ItemStack(m, count);
        ItemMeta im = it.getItemMeta();

        im.setDisplayName("§6§l"+ Utils.c(name));

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

    public static ItemStack simpleButton(ItemStack it, String name, String... lore) {
        ItemMeta im = it.getItemMeta();

        im.setDisplayName("§6§l"+Utils.c(name));
        im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

    public static ItemStack simpleButton(Material m, Player player, String name, String... lore) {
        ItemStack it = new ItemStack(m, 1);
        SkullMeta im = (SkullMeta) it.getItemMeta();
        im.setOwningPlayer(player);
        im.setDisplayName("§6§l"+Utils.c(name));

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

    private static void reorganizeItems(Player player, Inventory inv) {
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        MineSkyGuildas.getInstance().getPlayerData().getKills(player.getUniqueId(), kills -> {
            MineSkyGuildas.getInstance().getPlayerData().getDeaths(player.getUniqueId(), deaths -> {
                double kdr = (deaths == 0 ? kills : ((double) kills / deaths));

                inv.setItem(0, simpleButton(
                        Material.PLAYER_HEAD,
                        player,
                        player.getName(),
                        "• Suas informações de guilda.",
                        " ",
                        "&6Guilda: &e" + (guild == null ? "Sem Guilda" : guild.getName()) +
                                (guild == null ? "" : " &6[&f" + guild.getTag() + "&6]"),
                        "&6Cargo: &e" + (guild == null ? "Nenhum" : GuildRoles.getLabelRole(guild.getRole(player.getUniqueId()))),
                        "&6Mortes: &e" + deaths,
                        "&6Kills: &e" + kills,
                        "&6KDR: &e" + new DecimalFormat("0.00").format(kdr)
                ));
            });
        });

        Utils.getGuildInfo(player, itemStack -> inv.setItem(1, itemStack));
        inv.setItem(3, simpleButton(
                Material.PLAYER_HEAD, "Jogadores", "• Veja todos os jogadores do servidor.",
                "",
                "&e➳ Clique esquerdo - Para abrir o menu."
        ));
        inv.setItem(4, simpleButton(
                Material.PAINTING, "Guildas", "• Veja todas as guildas do servidor.",
                "",
                "&e➳ Clique esquerdo - Para abrir o menu."
        ));
        inv.setItem(8, simpleButton(
                Material.BOOK, "Comandos", "• Veja todos os comandos do servidor.",
                "",
                "&e➳ Clique esquerdo - Para visualizar."
        ));

    }

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "MineSky - Menu de guildas");

        inventories.put(player, inv);

        reorganizeItems(player, inv);

        player.openInventory(inv);
    }

    public static void reopenInventory(Player player) {
        Inventory inv = inventories.get(player);
        if(inv == null)
            return;

        reorganizeItems(player, inv);

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

        if(!inventories.containsValue(e.getInventory()))
            return;

        e.setCancelled(true);
        switch (slot) {
            case 1 -> {
                Guilds g = GuildHandler.getGuildByPlayer(p);
                if (g == null) {
                    GuildCreateMenu.openMainMenu(p, new GuildBuilder(p.getUniqueId()));
                }
                GuildInfoMenu.openMainMenu(p);
            }
            case 3 -> {
                PlayerListMenu.openMainMenu(p);
            }
            case 4 -> {
                GuildListMenu.openMainMenu(p);
            }
            case 8 -> {
                p.closeInventory();
                GuildCommand.commandList(p);
            }
        }
    }
}
