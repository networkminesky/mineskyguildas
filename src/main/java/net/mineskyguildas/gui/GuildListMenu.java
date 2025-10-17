package net.mineskyguildas.gui;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GuildListMenu implements Listener {
    private final MineSkyGuildas plugin;
    public static HashMap<Player, Inventory> inventories = new HashMap<>();
    public static HashMap<Player, Integer> playerPages = new HashMap<>();

    public GuildListMenu(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    public static ItemStack simpleButton(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Utils.c("&6&l" + name));
        im.setLore(Arrays.stream(lore).map(a -> Utils.c("&7" + a)).collect(Collectors.toList()));
        it.setItemMeta(im);
        return it;
    }
/*
    public static void reorganizeItems(Player player, int page) {
        List<Guilds> guildList = new ArrayList<>(GuildHandler.getGuilds().values());

        int guildsPerPage = 45;
        int maxPage = (int) Math.ceil((double) guildList.size() / guildsPerPage);
        if (maxPage == 0) maxPage = 1;

        page = Math.max(1, Math.min(page, maxPage));
        playerPages.put(player, page);

        Inventory inv = Bukkit.createInventory(null, 54, Utils.c("§8Guildas — Página " + page + "/" + maxPage));
        inventories.put(player, inv);

        int start = (page - 1) * guildsPerPage;
        int end = Math.min(start + guildsPerPage, guildList.size());

        for (int i = start; i < end; i++) {
            Guilds g = guildList.get(i);

            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.c("&6&l" + g.getName() + " &6[&f" + g.getTag() + "&6]"));

            List<String> lore = new ArrayList<>();
            lore.add(Utils.c("&6Descrição: &e" + (g.getDescription() == null ? "Sem descrição" : g.getDescription())));
            lore.add(Utils.c("&6Level: &e" + g.getLevel()));
            lore.add(Utils.c("&6XP: &e" + g.getXp() + "/" + g.xpRequiredForNextLevel()));
            lore.add(Utils.c("&6Líder: &e" + Bukkit.getOfflinePlayer(g.getLeader()).getName()));
            lore.add(Utils.c("&6Membros: &e" + GuildHandler.getOnlineMembers(g)));
            lore.add(Utils.c("&6Rivais: &e" + (g.getRivals().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getRivalsTags(g)))));
            lore.add(Utils.c("&6Aliados: &e" + (g.getAllies().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getAlliesTags(g)))));
            lore.add(Utils.c(" "));
            lore.add(Utils.c("&e➳ Clique esquerdo - Ver detalhes"));
            meta.setLore(lore);

            item.setItemMeta(meta);
            inv.addItem(item);
        }

        inv.setItem(45, simpleButton(Material.ARROW, "Página Anterior", "Voltar uma página."));
        inv.setItem(49, simpleButton(Material.BARRIER, "Fechar", "Fechar o menu."));
        inv.setItem(53, simpleButton(Material.ARROW, "Próxima Página", "Avançar uma página."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }*/
}
