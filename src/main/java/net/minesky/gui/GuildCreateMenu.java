package net.minesky.gui;

import net.minesky.MineSkyGuildas;
import net.minesky.api.events.GuildCreateEvent;
import net.minesky.builders.GuildBuilder;
import net.minesky.config.Config;
import net.minesky.handlers.GuildHandler;
import net.minesky.hooks.Vault;
import net.minesky.utils.ChatInputCallback;
import net.minesky.utils.Utils;
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

public class GuildCreateMenu implements Listener {
    private final MineSkyGuildas plugin;
    private static final HashMap<Player, GuildBuilder> builderHashMap = new HashMap<>();
    public static HashMap<Player, Inventory> inventories = new HashMap<>();


    public GuildCreateMenu(MineSkyGuildas plugin) {
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

    private static void reorganizeItems(GuildBuilder builder, Inventory inv, Player player) {
        inv.setItem(10, simpleButton(
                Material.NAME_TAG, "Nome", "• Define um nome de exibição", " para sua guilda.",
                " ",
                "&6Nome: &e" +( builder.getDisplayName() == null || builder.getDisplayName().isEmpty() ? "Sem nome" : builder.getDisplayName()),
                " ",
                "&e➳ Clique esquerdo - Definir nome",
                "&e➳ Clique direito - Remover nome")
        );

        inv.setItem(13, simpleButton(
                Material.MAGMA_CREAM, "Tag", "• Define a tag da sua guilda",
                " ",
                "&6Tag: &e"+( builder.getTag() == null || builder.getTag().isEmpty() ? "Sem Tag" : builder.getTag()),
                " ",
                "&e➳ Clique esquerdo - Definir tag",
                "&e➳ Clique direito - Remover tag")
        );

        inv.setItem(16, simpleButton(
                (builder.getDisplayName() == null || builder.getTag() == null || Vault.getBalance(player) < 1500 ? Material.RED_WOOL : Material.GREEN_WOOL), "Salvar", "• Salvar a sua configuração", " da sua guilda",
                " ",
                "&6Nome: &e" +( builder.getDisplayName() == null || builder.getDisplayName().isEmpty() ? "Sem nome" : builder.getDisplayName()),
                "&6Tag: &e"+( builder.getTag() == null || builder.getTag().isEmpty() ? "Sem Tag" : builder.getTag()),
                "&6Lider: &e"+( builder.getLider() != null ? builder.getLider().getName() : "Não encontrado"),
                " ",
                "&6Preço: &e$" + Config.GuildCreatePrice,
                " ",
                "&e➳ Clique esquerdo - Para salvar a guilda",
                "&e➳ Clique direito - Para cancelar")
        );
    }

    public static void openMainMenu(Player player, GuildBuilder builder) {
        Inventory inv = Bukkit.createInventory(null, 27, "Configuração da Guilda.");

        builderHashMap.put(player, builder);
        inventories.put(player, inv);

        reorganizeItems(builder, inv, player);

        player.openInventory(inv);
    }

    public static void reopenInventory(Player player) {
        Inventory inv = inventories.get(player);
        GuildBuilder builder = builderHashMap.get(player);
        if(inv == null || builder == null)
            return;

        reorganizeItems(builder, inv, player);

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

        GuildBuilder builder = builderHashMap.get(p);
        assert builder != null;

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
                            if (GuildHandler.doesGuildNameExist(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c❌ Ops! Já existe uma guilda com esse nome. Tente outro nome!"));
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
                            if (!Utils.isValidTag(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c⚠ A tag precisa ter até " + Config.GuildTagLimit + " caracteres. Tente uma curta!"));
                                return;
                            }
                            if (GuildHandler.doesGuildTagExist(response)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c❌ Essa tag já está em uso por outra guilda. Tente uma diferente!"));
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
                            p.sendMessage(Utils.c("&c⚠ Você ainda não escolheu o nome e a tag da sua guilda! Personalize antes de salvar."));
                            return;
                        }

                        if (!Vault.withdraw(p, Config.GuildCreatePrice)) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                            p.sendMessage(Utils.c("&4\uD83D\uDCB5 &cVocê precisa de &4$" + Config.GuildCreatePrice + "&c para criar sua guilda&4."));
                            return;
                        }

                        String ID = builder.generateId();
                        GuildCreateEvent event = new GuildCreateEvent(p, ID, builder.getDisplayName(), builder.getTag());
                        plugin.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            p.closeInventory();
                            builderHashMap.remove(p);
                            p.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A criação da guilda foi interrompida pela API.") : event.CancelledMessage));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                            return;
                        }
                        GuildHandler.createGuilda(ID, builder.getDisplayName(), builder.getTag(), builder.getLider());
                        Bukkit.broadcastMessage(Utils.c("&3\uD83C\uDFF4 &b" + builder.getLider().getName() + " &3fundou a guilda &f" + builder.getDisplayName() + "&b!"));
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
        }

    }
}
