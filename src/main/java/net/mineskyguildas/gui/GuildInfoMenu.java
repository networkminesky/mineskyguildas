package net.mineskyguildas.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.commands.GuildCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GuildInfoMenu implements Listener {
    private final MineSkyGuildas plugin;
    public static HashMap<Player, Inventory> inventories = new HashMap<>();

    public GuildInfoMenu(MineSkyGuildas plugin) {
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

    private static void reorganizeItems(Player player, Inventory inv, Guilds g) {
        for (int i = 0; i <= 8; i++) {
            if (i == 4) continue;
            inv.setItem(i, simpleButton(Material.BLACK_STAINED_GLASS_PANE, "", ""));
        }
        inv.setItem(4, simpleButton(Material.RED_WOOL, "Voltar", "Voltar para o menu anterior."));

        Utils.getGuildInfoAsync(g, itemStack -> inv.setItem(13, itemStack));
        inv.setItem(19, simpleButton(Material.PLAYER_HEAD, "Membros", "• Visualizar todos os membros.", "", "&e➳ Clique esquerdo - Para visualizar."));
        inv.setItem(21, simpleButton(Material.COMPASS, "Coordenadas", "• Visualizar as coordenadas de todos os membros.", "", "&e➳ Clique esquerdo - Para visualizar."));
        inv.setItem(23, simpleButton(Material.CYAN_BANNER, "Aliados", "• Gerenciar os aliados.", "", "&e➳ Clique esquerdo - Para adicionar um aliado.", "&e➳ Clique esquerdo - Para remover um aliado.", "&e➳ Drope - Para listar os aliados."));
        inv.setItem(25, simpleButton(Material.RED_BANNER, "Rivais", "• Gerenciar os rivais.", "", "&e➳ Clique esquerdo - Para adicionar um rival.", "&e➳ Clique esquerdo - Para remover um rival.", "&e➳ Drope - Para listar os rivais."));
        inv.setItem(28, simpleButton(Material.LIME_BED, "Base", "• Gerenciar a base da guilda.", "", "&e➳ Clique esquerdo - Para se teleportar.", "&e➳ Clique esquerdo - Para setar um novo local.", "&e➳ Drope - Para remover a base."));
        inv.setItem(30, simpleButton(Material.BEACON, "Reagrupar", "• Um pedido de teleporte para todos da guilda.", "", "&e➳ Clique esquerdo - Para enviar para a sua localização.", "&e➳ Clique direito - Para enviar para a base da guilda."));
        inv.setItem(32, simpleButton(Material.NETHERITE_SWORD, "Fogo-amigo", "• Gerenciar o fogo amigo da guilda.", "", "&e➳ Clique esquerdo - Para alterar o status do fogo-amigo."));
        inv.setItem(34, simpleButton(Material.RAW_GOLD, "Banco", "• Banco da guilda.", "", "&6Saldo: &e$" + g.getBalance(), "", "&e➳ Clique esquerdo - Para depositar.", "&e➳ Clique direito - Para sacar."));
        inv.setItem(40, simpleButton(Material.KNOWLEDGE_BOOK, "Chat", "• Enviar mensagem para a guilda.", "", "&6/. <mensagem> &e- Chat da guilda", "&6/ally <mensagem> &e- Chat de aliados da guilda.", "&6/lideres <mensagem> &e- Chat exclusivo dos líderes da guilda."));
        inv.setItem(48, simpleButton(Material.IRON_DOOR, "Abandonar", "• Sair da sua guilda.", "", "&e➳ Clique esquerdo - Para sair da guilda."));
        inv.setItem(50, simpleButton(Material.REDSTONE, "Acabar", "• Acabar com a guilda.", "", "&e➳ Drope - Para acabar com a guilda."));
    }

    public static void openMainMenu(Player player) {
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            GuildCreateMenu.openMainMenu(player, new GuildBuilder(player.getUniqueId()));
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, Utils.c("[" + guild.getTag() + "&r] " + guild.getName()));

        inventories.put(player, inv);

        reorganizeItems(player, inv, guild);

        player.openInventory(inv);
    }

    public static void reopenInventory(Player player) {
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            GuildCreateMenu.openMainMenu(player, new GuildBuilder(player.getUniqueId()));
            return;
        }
        Inventory inv = inventories.get(player);
        if(inv == null)
            return;

        reorganizeItems(player, inv, guild);

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
            case 4 -> {
                GuildMenu.openMainMenu(p);
            }
            case 19 -> {
                p.closeInventory();
                p.performCommand("guild membros");
            }
            case 21 -> {
                p.closeInventory();
                p.performCommand("guild coordenadas");
            }
            case 23 -> {
                switch (clickType) {
                    case LEFT -> {
                        p.closeInventory();
                        Utils.awaitChatInput(p, new ChatInputCallback() {
                            @Override
                            public void onInput(String response) {
                                p.performCommand("guild aliado adicionar " + response);
                            }

                            @Override
                            public void onCancel() {
                                reopenInventory(p);
                            }
                        });
                    }
                    case RIGHT -> {
                        p.closeInventory();
                        Utils.awaitChatInput(p, new ChatInputCallback() {
                            @Override
                            public void onInput(String response) {
                                p.performCommand("guild aliado remover " + response);
                            }

                            @Override
                            public void onCancel() {
                                reopenInventory(p);
                            }
                        });
                    }
                    case DROP -> {
                        p.closeInventory();
                        p.performCommand("guild aliado list");
                    }
                }
            }
             case 25 -> {
                 switch (clickType) {
                     case LEFT -> {
                         p.closeInventory();
                         Utils.awaitChatInput(p, new ChatInputCallback() {
                             @Override
                             public void onInput(String response) {
                                 p.performCommand("guild rival adicionar " + response);
                             }

                             @Override
                             public void onCancel() {
                                 reopenInventory(p);
                             }
                         });
                     }
                     case RIGHT -> {
                         p.closeInventory();
                         Utils.awaitChatInput(p, new ChatInputCallback() {
                             @Override
                             public void onInput(String response) {
                                 p.performCommand("guild rival remover " + response);
                             }

                             @Override
                             public void onCancel() {
                                 reopenInventory(p);
                             }
                         });
                     }
                     case DROP -> {
                         p.closeInventory();
                         p.performCommand("guild rival list");
                     }
                 }
             }
             case 28 -> {
                 p.closeInventory();
                switch (clickType) {
                    case LEFT -> {
                        p.performCommand("guild base teleportar");
                    }
                    case RIGHT -> {
                        p.performCommand("guild base setar");
                    }
                    case DROP -> {
                        p.sendMessage(Utils.c("&4⚠ &cSistema não implementado!"));
                        reopenInventory(p);
                    }
                }
             }
             case 30 -> {
                p.closeInventory();
                switch (clickType) {
                    case LEFT -> {
                        p.performCommand("guild reagrupar");
                    }
                    case RIGHT -> {
                        p.performCommand("guild reagrupar base");
                    }
                }
             }
             case 32 -> {
                 p.performCommand("guild fogo-amigo");
             }
             case 34 -> {
                switch (clickType) {
                    case LEFT -> {
                        p.closeInventory();
                        Utils.awaitChatInput(p, new ChatInputCallback() {
                            @Override
                            public void onInput(String response) {
                                p.performCommand("guild banco depositar " + response);
                            }

                            @Override
                            public void onCancel() {
                                reopenInventory(p);
                            }
                        });
                    }
                    case RIGHT -> {
                        p.closeInventory();
                        Utils.awaitChatInput(p, new ChatInputCallback() {
                            @Override
                            public void onInput(String response) {
                                p.performCommand("guild banco sacar " + response);
                            }

                            @Override
                            public void onCancel() {
                                reopenInventory(p);
                            }
                        });
                    }
                }
             }
             case 48 -> {
                p.closeInventory();
                p.performCommand("guild sair");
             }
             case 50 -> {
                switch (clickType) {
                    case DROP -> {
                        p.closeInventory();
                        p.performCommand("guild acabar");
                    }
                }
             }
        }
    }
}
