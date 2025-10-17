package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
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
        inv.setItem(0, simpleButton(
                Material.PLAYER_HEAD, player, player.getName(), "• Sua informações de guilda.",
                " ",
                "&6Guilda: &e" +( guild == null ? "Sem Guilda" : guild.getName()) + ( guild == null ? "" : " &6[&f" + guild.getTag() + "&6]"),
                "&6Cargo: &e" + (guild == null ? "Nenhum" : GuildRoles.getLabelRole(guild.getRole(player.getUniqueId())))
        ));
        ItemStack item = (guild != null && guild.getBanner() != null)
                ? guild.getBanner()
                : new ItemStack(Material.GREEN_BANNER);

        String titulo = (guild == null ? "Agente Livre" : guild.getName() + " &6[&f" + guild.getTag() + "&6]");

        String[] lore = (guild == null)
                ? new String[] {
                "• Você não está em uma guilda.",
                " ",
                "&6Preço: &e1,500",
                " ",
                "&e➳ Clique esquerdo - Para criar uma guilda."
        }
                : new String[] {
                "• Informações da guilda.",
                " ",
                "&6Descrição: &e" + (guild.getDescription() == null ? "Sem descrição" : guild.getDescription()),
                "&6Level: &e" + guild.getLevel(),
                "&6XP: &e" + guild.getXp() + "/" + guild.xpRequiredForNextLevel(),
                "&6Líder: &e" + Bukkit.getOfflinePlayer(guild.getLeader()).getName(),
                "&6Membros: &e" + GuildHandler.getOnlineMembers(guild),
                "&6Rivais: &e" + (guild.getRivals().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getRivalsTags(guild))),
                "&6Aliados: &e" + (guild.getAllies().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getAlliesTags(guild))),
                " "
        };

        inv.setItem(1, simpleButton(item, titulo, lore));

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
/*
        switch(slot) {
            // Modificar nome do item
            case 10 -> {
                switch(clickType) {
                    case RIGHT -> {
                        builder.setName(null);
                        return;
                    }
                    case LEFT -> Utils.awaitChatInput(p, new ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            GuildasHandler handler = new GuildasHandler(plugin);
                            if (handler.isGuildaName(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&cJá existe uma guilda com esse nome."));
                                reopenInventory(p);
                                return;
                            }
                            builder.setName(response);
                            reopenInventory(p);
                        }

                        @Override
                        public void onCancel() {
                            reopenInventory(p);
                        }
                    });
                }
            }

            // Global
            case 13 -> {
                switch(clickType) {
                    case RIGHT -> {
                        builder.setTag(null);
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);
                        reopenInventory(p);
                        return;
                    }
                    case LEFT -> Utils.awaitChatInput(p, new ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            GuildasHandler handler = new GuildasHandler(plugin);
                            if (!Utils.isValidTag(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&cA tag deve ter menos de 4 caracteres."));
                                return;
                            }
                            if (handler.isGuildaTag(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&cJá existe uma guilda com esse tag."));
                                reopenInventory(p);
                                return;
                            }
                            builder.setTag(response);
                            reopenInventory(p);
                        }

                        @Override
                        public void onCancel() {
                            reopenInventory(p);
                        }
                    });
                }
            }


            // Salvar
            case 16 -> {
                switch(clickType) {
                    case RIGHT -> {
                        builderHashMap.remove(p);
                        p.closeInventory();
                        return;
                    }
                    case LEFT -> {
                        if (builder.getTag() == null | builder.getDisplayName() == null) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                            p.sendMessage(Utils.c("&cVocê precisa definir um nome e uma tag para sua guilda."));
                            return;
                        }
                        GuildasHandler handler = new GuildasHandler(plugin);
                        handler.createGuilda(builder.generateId(), builder.getDisplayName(), builder.getTag(), builder.getLider());
                        Bukkit.broadcastMessage(Utils.c("&b" + builder.getLider().getName() + " criou a " + builder.getDisplayName()));
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);
                        builderHashMap.remove(p);
                        p.closeInventory();
                        return;
                    }
                }
            }
        }

        switch(clickType) {
            case RIGHT -> {
                p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 0);
                reopenInventory(p);
            }
            case LEFT -> p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);
        }*/

    }

   /* private static ItemStack simpleButton(Material m, String name, String... lore) {
        return simpleButton(m, name, 1, lore);
    }
    private static ItemStack simpleButton(Material m, String name, int count, String... lore) {
        ItemStack it = new ItemStack(m, count);
        ItemMeta im = it.getItemMeta();

        im.setDisplayName(Utils.c(name));
        im.setLore(Arrays.asList(lore));

        it.setItemMeta(im);
        return it;
    }
    public Inventory mainMenu(Player player) {
        UUID guildaId = plugin.getGuildasBuilder().getGuildaPorJogador(player.getUniqueId());

        if (guildaId == null) {
            return mainMenu(player);
        }

        String guildaNome = plugin.getGuildasBuilder().getNomeGuilda(guildaId);
        Player guildaLider = plugin.getGuildasBuilder().getLiderGuilda(guildaId);
        String guildaTag = plugin.getGuildasBuilder().getTagGuilda(guildaId);
        String cargo = plugin.getGuildasBuilder().getCargo(guildaId, player.getUniqueId()).name();
        int nivel = plugin.getGuildasBuilder().getLevelGuilda(guildaId);
        Inventory inv = Bukkit.createInventory(null, 9, "Menu das Guildas.");
        inv.setItem(0, playerHead(player, guildaLider, guildaNome, guildaTag, cargo, nivel));

        return inv;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

    }

    public Inventory mainMenuOFF(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Menu das Guildas.");
        inv.setItem(0, playerHead(player));

        return inv;
    }

    private static ItemStack playerHead(Player player, Player lider, String guildaNome, String guildaTag, String cargo, int nivel) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(Utils.c("&e" + player.getName() + " informações"));
        meta.setLore(Arrays.asList(
                Utils.c("&7Guilda: &e" + guildaNome + " [" + guildaTag + "]"),
                Utils.c("&7Lider: &e" + lider.getName()),
                Utils.c("&7Cargo: &e" + cargo),
                Utils.c("&7Nível da Guilda: &e" + nivel)
        ));
        head.setItemMeta(meta);
        return head;
    }

    private static ItemStack playerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(Utils.c("&e" + player.getName() + " informações"));
        meta.setLore(Arrays.asList(
                Utils.c("&7Guilda: &cSem Guilda")
        ));
        head.setItemMeta(meta);
        return head;
    }*/
}
