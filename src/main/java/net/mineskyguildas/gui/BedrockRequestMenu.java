package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
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

public class BedrockRequestMenu implements Listener {
    private final MineSkyGuildas plugin;

    private static final Map<UUID, BedrockInviteData> activeMenus = new ConcurrentHashMap<>();

    public BedrockRequestMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static class BedrockInviteData {
        private final Guilds requester;
        private final GuildRequestType type;

        public BedrockInviteData(Guilds requester, GuildRequestType type) {
            this.requester = requester;
            this.type = type;
        }

        public Guilds getRequester() { return requester; }
        public GuildRequestType getType() { return type; }
    }

    public static void openMenu(Player player, Guilds requester, GuildRequestType type) {
        UUID uuid = player.getUniqueId();
        activeMenus.put(uuid, new BedrockInviteData(requester, type));

        String title = type == GuildRequestType.WAR ? "§4⚔ Desafio de Guerra!" : "§b📩 Pedido recebido!";
        Inventory inv = Bukkit.createInventory(null, 9, title);

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
            acceptMeta.setDisplayName(Utils.c("&a&l[✔ Aceitar]"));
            acceptMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para CONFIRMAR"),
                    Utils.c("&7e aceitar o convite.")
            ));
            acceptButton.setItemMeta(acceptMeta);
        }
        inv.setItem(1, acceptButton);

        ItemStack infoBook = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(Utils.c("&b&lInformações do Pedido"));

            List<String> lore = new ArrayList<>();
            lore.add(Utils.c("&7"));
            lore.add(Utils.c("&bClã Solicitante: &3" + requester.getName() + " &b[&f" + requester.getTag() + "&b]"));

            if (type == GuildRequestType.WAR) {
                lore.add(Utils.c("&cTipo: &4&lGUERRA"));
                lore.add(Utils.c("&7"));
                lore.add(Utils.c("&e⚠ Territórios expostos a invasões!"));
                if (requester.isAlly(GuildHandler.getGuildByPlayer(player))) {
                    lore.add(Utils.c("&e⚠ Entrará ativamente na guerra como apoiador!"));
                } else {
                    lore.add(Utils.c("&a✔ Todo o saldo de banco do inimigo!"));
                    lore.add(Utils.c("&a✔ Recompensa de 10.000 XP!"));
                    lore.add(Utils.c("&c⚠ Se perder, seu clã será destruído!"));
                }
            } else if (type == GuildRequestType.ALLY) {
                lore.add(Utils.c("&bTipo: &3Pedido de Aliança"));
                lore.add(Utils.c("&7"));
                lore.add(Utils.c("&a✔ Fogo-Amigo desativado!"));
                lore.add(Utils.c("&a✔ Cooperação mútua!"));
            } else {
                lore.add(Utils.c("&eTipo: &6Encerrar Rivalidade (Paz)"));
            }

            infoMeta.setLore(lore);
            infoMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            infoBook.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoBook);

        ItemStack rejectButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta rejectMeta = rejectButton.getItemMeta();
        if (rejectMeta != null) {
            rejectMeta.setDisplayName(Utils.c("&c&l[❌ Rejeitar]"));
            rejectMeta.setLore(Arrays.asList(
                    Utils.c("&7"),
                    Utils.c("&7Clique para RECUSAR"),
                    Utils.c("&7e rejeitar o convite.")
            ));
            rejectButton.setItemMeta(rejectMeta);
        }
        inv.setItem(7, rejectButton);

        player.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (activeMenus.containsKey(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        activeMenus.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (!activeMenus.containsKey(uuid)) return;
        e.setCancelled(true);

        BedrockInviteData data = activeMenus.get(uuid);

        int slot = e.getSlot();
        String cleanTag = Utils.getTag(data.getRequester().getTag());

        if (slot == 1) {
            if (data.getType() == GuildRequestType.WAR) {
                player.performCommand("clan guerra aceitar " + cleanTag);
                player.closeInventory();
                activeMenus.remove(uuid);
            } else {
                player.performCommand("clan aceitar");
                player.closeInventory();
                activeMenus.remove(uuid);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else if (slot == 7) {
            if (data.getType() == GuildRequestType.WAR) {
                player.performCommand("clan guerra recusar " + cleanTag);
                player.closeInventory();
                activeMenus.remove(uuid);
            } else {
                player.performCommand("clan rejeitar");
                player.closeInventory();
                activeMenus.remove(uuid);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}