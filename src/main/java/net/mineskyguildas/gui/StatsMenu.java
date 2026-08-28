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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsMenu implements Listener {
    private final MineSkyGuildas plugin;

    public static final Map<UUID, Inventory> inventories = new HashMap<>();
    public static final Map<UUID, UUID> viewingTargets = new HashMap<>();

    public StatsMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    private static String parse(Player target, String text) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(target, text);
        }
        return Utils.c(text);
    }

    public static ItemStack papiButton(Material m, Player target, String name, String... lore) {
        ItemStack it = new ItemStack(m, 1);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            String formattedName = parse(target, name);
            im.setDisplayName(formattedName.startsWith("§") ? formattedName : "§b§l" + formattedName);
            im.setLore(Arrays.stream(lore)
                    .map(line -> parse(target, "&7" + line))
                    .collect(Collectors.toList()));
            im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            it.setItemMeta(im);
        }
        return it;
    }

    public static ItemStack papiHeadButton(Player target, String name, String... lore) {
        ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) it.getItemMeta();
        if (im != null) {
            im.setOwningPlayer(target);
            String formattedName = parse(target, name);
            im.setDisplayName(formattedName.startsWith("§") ? formattedName : "§b§l" + formattedName);
            im.setLore(Arrays.stream(lore)
                    .map(line -> parse(target, "&7" + line))
                    .collect(Collectors.toList()));
            im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            it.setItemMeta(im);
        }
        return it;
    }

    public static ItemStack backgroundFiller() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(" ");
            im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            it.setItemMeta(im);
        }
        return it;
    }

    private static void reorganizeItems(Player viewer, Player target, Inventory inv) {
        Guilds guild = GuildHandler.getGuildByPlayer(target.getUniqueId());

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, backgroundFiller());
        }

        inv.setItem(10, papiHeadButton(
                target,
                "&b&lPERFIL DE " + target.getName().toUpperCase(),
                "• Informações básicas do jogador.",
                "",
                "&fGrupo atual: &7" + ("%luckperms_prefix%".equals("&7") ? "Membro" : "%luckperms_prefix%"),
                "&fTempo de jogo: &e%statistic_time_played%",
                "&fData de entrada: &e%player_first_join_date%"
        ));

        inv.setItem(12, papiButton(
                Material.GOLD_INGOT,
                target,
                "&e&lFINANÇAS",
                "• Balanço financeiro atual deste jogador.",
                "",
                "&fSaldo: &a%vault_eco_balance_formatted%"
        ));

        // Slot 13: AuraSkills do Alvo
        inv.setItem(13, papiButton(
                Material.BREWING_STAND,
                target,
                "&d&lPODER",
                "• Habilidades e atributos deste jogador.",
                "",
                "&fNível de Poder: &d%auraskills_power%",
                "&fMana Atual: &b%auraskills_mana%",
                "&fVida máxima: &c%auraskills_hp%"
        ));

        MineSkyGuildas.getInstance().getPlayerData().getKills(target.getUniqueId(), kills -> {
            MineSkyGuildas.getInstance().getPlayerData().getDeaths(target.getUniqueId(), deaths -> {
                double kdr = (deaths == 0 ? kills : ((double) kills / deaths));

                ItemStack combatItem = papiButton(
                        Material.NETHERITE_SWORD,
                        target,
                        "&c&lCOMBATE",
                        "• Histórico de combate deste jogador.",
                        "",
                        "&fKills (Jogadores): &c" + (int) kills,
                        "&fMortes: &c" + (int) deaths,
                        "&fKDR (Média): &e" + new DecimalFormat("0.00").format(kdr),
                        "&fMonstros derrotados: &7%statistic_mob_kills%"
                );

                viewer.getScheduler().run(MineSkyGuildas.getInstance(), task -> inv.setItem(14, combatItem), null);
            });
        });

        inv.setItem(16, papiButton(
                Material.SHIELD,
                target,
                "&3&lGUILDA / CLÃ",
                "• Filiação a clãs deste jogador.",
                "",
                "&fClã: &b" + (guild == null ? "Nenhum" : guild.getName() + " &8[&f" + guild.getTag() + "&8]"),
                "&fCargo: &b" + (guild == null ? "Nenhum" : GuildRoles.getLabelRole(guild.getRole(target.getUniqueId()))),
                "",
                "&e➳ Clique esquerdo - Para abrir o menu de clã."
        ));

        inv.setItem(22, papiButton(Material.BARRIER, target, "&c&lFechar Menu", "• Clique para fechar esta interface."));
    }

    public static void openMainMenu(Player viewer, Player target) {
        String title = viewer.equals(target) ? "MineSky - Seus Status" : "MineSky - Status de " + target.getName();
        Inventory inv = Bukkit.createInventory(null, 27, title);

        inventories.put(viewer.getUniqueId(), inv);
        viewingTargets.put(viewer.getUniqueId(), target.getUniqueId());

        reorganizeItems(viewer, target, inv);
        viewer.openInventory(inv);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (inventories.containsValue(e.getInventory())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!inventories.containsValue(e.getInventory())) return;

        e.setCancelled(true);
        final int slot = e.getSlot();

        switch (slot) {
            case 16 -> {
                p.closeInventory();
                GuildMenu.openMainMenu(p);
            }
            case 22 -> p.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        inventories.remove(p.getUniqueId());
        viewingTargets.remove(p.getUniqueId());
    }
}