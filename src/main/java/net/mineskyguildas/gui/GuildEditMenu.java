package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.GuildCreateEvent;
import net.mineskyguildas.builders.GuildBuilder;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.Vault;
import net.mineskyguildas.utils.ChatInputCallback;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static net.mineskyguildas.commands.GuildCommand.sendError;

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

        im.setDisplayName("§b§l"+name);

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

    private static void reorganizeItems(Inventory inv, Player player, Guilds g) {
        inv.setItem(11, simpleButton(
                Material.MAGMA_CREAM, "Tag", "• Define a tag do seu clã",
                " ",
                "&bTag: &3"+( g.getTag() == null || g.getTag().isEmpty() ? "Sem Tag" : g.getTag()),
                " ",
                "&e➳ Clique esquerdo - Alterar tag")
        );

        inv.setItem(15, simpleButton(
                Material.BOOK, "Descrição", "• Altere a descrição",
                " do seu clã",
                " ",
                "&bDescrição: &3"+( g.getDescription() == null || g.getDescription().isEmpty() ? "Sem descrição" : g.getDescription()),
                " ",
                "&e➳ Clique esquerdo - Alterar descrição",
                "&e➳ Clique direito - Remover descrição")
        );

        OfflinePlayer currentLeader = g.getLeader() != null ? Bukkit.getOfflinePlayer(g.getLeader()) : null;
        String leaderName = (currentLeader != null && currentLeader.getName() != null) ? currentLeader.getName() : "Nenhum";

        inv.setItem(13, simpleButton(
                Material.PLAYER_HEAD, "Liderança", "• Transfira a liderança",
                " do seu clã para outro jogador",
                " ",
                "&bLíder atual: &3" + leaderName,
                " ",
                "&e➳ Drope - Transferir liderança")
        );
    }

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Configuração do clã.");

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
            case 11 -> {
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
                                reopenInventory(p);
                                return;
                            }
                            if (GuildHandler.doesGuildTagExist(response, g.getId())) {
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                                p.sendMessage(Utils.c("&c❌ Ops! Já existe um clã com essa Tag. Tente outra tag!"));
                                reopenInventory(p);
                                return;
                            }
                            g.setTag(response);
                            GuildHandler.saveGuildas();
                            MineSkyGuildas.l.info("[Clãs] " + p.getName() + " editou a tag do clã " + g.getName() + " para  " + Utils.getTag(g.getTag()));
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

            case 13 -> {
                switch (clickType) {
                    case DROP -> {
                        GuildRoles role = g.getRole(p.getUniqueId());
                        if (!role.equals(GuildRoles.LEADER)) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            p.sendMessage(Utils.c("&c⚠ Você não pode alterar o líder do seu clã"));
                            reopenInventory(p);
                            return;
                        }

                        if (MineSkyGuildas.getInstance().getWarHandler().isGuildInActiveWar(g.getId())
                                || MineSkyGuildas.getInstance().getWarHandler().isGuildInWarOrPending(g.getId())) {
                            sendError(p, "&4⚠ &cSeu clã está em guerra e não pode trocar o líder.");
                            return;
                        }

                        Utils.awaitChatInput(p, new ChatInputCallback() {
                            @Override
                            public void onInput(String response) {
                                OfflinePlayer targetPlayer = Bukkit.getPlayerExact(response);

                                if (targetPlayer == null) {
                                    targetPlayer = Bukkit.getOfflinePlayerIfCached(response);
                                }

                                if (targetPlayer == null) {
                                    targetPlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                                            .filter(op -> op.getName() != null && op.getName().equalsIgnoreCase(response))
                                            .findFirst()
                                            .orElse(null);
                                }

                                if (targetPlayer == null) {
                                    targetPlayer = Bukkit.getOfflinePlayer(response);
                                }

                                if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                                    p.sendMessage(Utils.c("&c❌ O jogador '" + response + "' nunca entrou no servidor!"));
                                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    return;
                                }

                                UUID newLeaderUuid = targetPlayer.getUniqueId();
                                UUID oldLeaderUUID = g.getLeader();

                                if (newLeaderUuid.equals(oldLeaderUUID)) {
                                    p.sendMessage(Utils.c("&c⚠ Este jogador já é o líder supremo do clã!"));
                                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    return;
                                }

                                MemberData newLeaderData = g.getMemberData(newLeaderUuid);
                                if (newLeaderData == null) {
                                    p.sendMessage(Utils.c("&c⚠ Este jogador não pertence ao seu clã!"));
                                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    return;
                                }

                                newLeaderData.setRole(GuildRoles.LEADER);

                                MemberData oldData = g.getMemberData(oldLeaderUUID);
                                if (oldData != null) {
                                    oldData.setRole(GuildRoles.SUB_LEADER);
                                }

                                g.setLeader(newLeaderUuid);

                                String targetName = (targetPlayer.getName() != null ? targetPlayer.getName() : response);

                                MineSkyGuildas.l.info("[Clãs] " + p.getName() + " transferiu o cargo de líder do clã " + g.getName() + " para o " + targetName);
                                GuildHandler.broadcastGuildMessage(g, "&3\uD83D\uDC51 &b" + targetName + " &3recebeu a liderança do clã.");

                                Player newLeaderOnline = targetPlayer.getPlayer();
                                if (newLeaderOnline != null && newLeaderOnline.isOnline()) {
                                    newLeaderOnline.sendMessage(Utils.c("&6⭐ Você foi promovido a líder do clã " + g.getName()));
                                }
                            }

                            @Override
                            public void onCancel() {
                                p.getScheduler().run(MineSkyGuildas.getInstance(), task -> reopenInventory(p), null);
                            }
                        });
                    }
                }
            }

            case 15 -> {
                switch(clickType) {
                    case RIGHT -> {
                        g.setDescription(null);
                        GuildHandler.saveGuildas();
                        MineSkyGuildas.l.info("[Clãs] " + p.getName() + " removeu a descrição do clã " + g.getName());
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);
                        reopenInventory(p);
                        return;
                    }
                    case LEFT -> Utils.awaitChatInput(p, new ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            g.setDescription(response);
                            GuildHandler.saveGuildas();
                            MineSkyGuildas.l.info("[Clãs] " + p.getName() + " editou a descrição do clã " + g.getName() + " para " + g.getDescription());
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
