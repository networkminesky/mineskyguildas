package net.mineskyguildas.utils;

import net.md_5.bungee.api.ChatColor;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.SuperVanishHook;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static String c(String s) {
        return hex(s);
    }

    private static String createHex(String hexString) {
        hexString = hexString.replace("&", "");
        return net.md_5.bungee.api.ChatColor.of(hexString).toString();
    }

    public static String hex(String message) {
        Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");
        Matcher matcher = hexPattern.matcher(message);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, createHex(matcher.group()));
        }

        matcher.appendTail(result);
        message = result.toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void awaitChatInput(Player player, ChatInputCallback callback) {
        player.sendTitle("§9§lDigite no chat", "§7Digite 'sair' para voltar!", 5, 60, 20);
        player.closeInventory();

        Listener listener = new ChatListener(player, callback);
        Bukkit.getServer().getPluginManager().registerEvents(listener, MineSkyGuildas.getInstance());
    }

    public static void confirmAction(Player player, String action, Runnable onConfirm) {
        player.sendMessage(Utils.c("&3\uD83D\uDEA8 &9&lATENÇÃO&7: Digite &e\"Confirmar\" &7no chat para " + action + "."));
        Utils.awaitChatInput(player, new ChatInputCallback() {
            public void onInput(String input) {
                if (input.equalsIgnoreCase("confirmar") || input.equalsIgnoreCase("sim")) {
                    onConfirm.run();
                } else {
                    player.sendMessage(Utils.c("&c❌ Cancelado."));
                }
            }

            public void onCancel() {
                player.sendMessage(Utils.c("&c⏳ Tempo esgotado. Cancelado."));
            }
        });
    }

    public static String serializeLocation(Location l) {
        if (l == null) return null;
        return l.getWorld().getName() + ','
                + l.getX()   + ','
                + l.getY()   + ','
                + l.getZ()   + ','
                + l.getYaw() + ','
                + l.getPitch();
    }

    public static Location deserializeLocation(String s) {
        if (s == null) return null;
        String[] location = s.split(",");
        return new Location(
                Bukkit.getWorld   (location[0]),
                Double.parseDouble(location[1]),
                Double.parseDouble(location[2]),
                Double.parseDouble(location[3]),
                Float .parseFloat (location[4]),
                Float .parseFloat (location[5]))
                ;
    }

   /* public static void awaitChatInput(Player player, ChatInputCallback callback) {
        player.sendTitle("§9§lDigite no chat", "§7Digite 'sair' para voltar!", 5, 60, 20);

        player.closeInventory();

        Listener listener = new Listener() {
            @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
            public void onChat(AsyncPlayerChatEvent e) {

                Bukkit.getScheduler().runTask(MineSkyGuildas.getInstance(), () -> {
                    if(!e.getPlayer().equals(player))
                        return;

                    final String msg = e.getMessage();

                    switch(msg.toLowerCase()) {
                        case "cancel":
                        case "cancelar":
                        case "close":
                        case "sair": {
                            callback.onCancel();

                            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                            e.getPlayer().sendMessage(Utils.c("&cCancelando e retornando ao menu..."));

                            AsyncPlayerChatEvent.getHandlerList().unregister(this);
                            return;
                        }
                    }

                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES,1, 1);

                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    callback.onInput(c(msg));
                });

            }
        };
        Bukkit.getServer().getPluginManager().registerEvents(listener, MineSkyGuildas.getInstance());
    }*/

    public static final String BLUE_COLOR = net.md_5.bungee.api.ChatColor.of("#3d85c6")+"";

    public static boolean isValidTag(String tag) {
        if (tag == null || tag.isEmpty()) return false;

        if (tag.toLowerCase().contains("&k") || tag.toLowerCase().contains("§k")) return false;

        if (tag.matches("(?i).*(&k).*")) return false;

        tag = tag.replaceAll("(?i)&#[A-Fa-f0-9]{6}", "");

        tag = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', tag));

        return tag.length() <= Config.GuildTagLimit;
    }



    public static String getTag(String tag) {
        if (tag == null || tag.isEmpty()) return null;

        tag = tag.replaceAll("(?i)&#[A-Fa-f0-9]{6}", "");
        tag = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', tag));

        return tag;
    }

    public static void getGuildInfo(Player p, Consumer<ItemStack> callback) {
        Guilds g = GuildHandler.getGuildByPlayer(p.getUniqueId());
        getGuildInfoAsync(g, callback);
    }

    public static void getGuildInfoAsync(Guilds g, Consumer<ItemStack> callback) {
        ItemStack it = (g != null && g.getBanner() != null)
                ? g.getBanner()
                : new ItemStack(Material.GREEN_BANNER);
        ItemMeta im = it.getItemMeta();

        String title = (g == null ? "Agente Livre" : g.getName() + " &6[&f" + g.getTag() + "&6]");
        im.setDisplayName("§6§l" + Utils.c(title));
        im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        if (g == null) {
            List<String> lore = Arrays.asList(
                    "&7• Você não está em uma guilda.",
                    " ",
                    "&6Preço: &e1,500",
                    " ",
                    "&e➳ Clique esquerdo - Para criar uma guilda."
            );
            im.setLore(lore.stream().map(Utils::c).collect(Collectors.toList()));
            it.setItemMeta(im);
            callback.accept(it);
            return;
        }

        GuildStatsManager.calculateGuildStats(g, (totalKills, totalDeaths, kdr) -> {
            List<String> lore = new ArrayList<>();
            lore.add("&7• Informações da guilda.");
            lore.add(" ");
            lore.add("&6Descrição: &e" + (g.getDescription() == null ? "Sem descrição" : g.getDescription()));
            lore.add("&6Level: &e" + g.getLevel());
            lore.add("&6XP: &e" + g.getXp() + "/" + g.xpRequiredForNextLevel());
            lore.add("&6Líder: &e" + Bukkit.getOfflinePlayer(g.getLeader()).getName());
            lore.add("&6Membros: &e" + GuildHandler.getOnlineMembers(g));
            lore.add("&6Kills totais: &e" + totalKills);
            lore.add("&6Mortes totais: &e" + totalDeaths);
            lore.add("&6KDR médio: &e" + new DecimalFormat("0.00").format(kdr));
            lore.add("&6Rivais: &e" + (g.getRivals().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getRivalsTags(g))));
            lore.add("&6Aliados: &e" + (g.getAllies().isEmpty() ? "Nenhum" : String.join("&6, &e", GuildHandler.getAlliesTags(g))));
            lore.add(" ");

            im.setLore(lore.stream().map(Utils::c).collect(Collectors.toList()));
            it.setItemMeta(im);

            callback.accept(it);
        });
    }



    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(b -> !SuperVanishHook.isPlayerVanished(b))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getOnlineGuildMembersNamePerPlayer(Player p) {
        Guilds g = GuildHandler.getGuildByPlayer(p.getUniqueId());
        if (g == null) return Collections.emptyList();

        return g.getMembers().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getGuildMembersNamePerPlayer(Player p) {
        Guilds g = GuildHandler.getGuildByPlayer(p.getUniqueId());
        if (g == null) return Collections.emptyList();

        return g.getMembers().keySet().stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getGuildMembersNamesPerTag(String tag) {
        Guilds g = GuildHandler.getGuildByTag(tag);
        if (g == null) return Collections.emptyList();

        return g.getMembers().keySet().stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getGuildsTags(Player p) {
        Guilds playerGuild = GuildHandler.getGuildByPlayer(p.getUniqueId());

        return GuildHandler.getGuilds().values().stream()
                .filter(g -> playerGuild == null || !g.getId().equals(playerGuild.getId()))
                .map(g -> getTag(g.getTag()))
                .toList();
    }

    public static List<String> getGuildsTags() {
        return GuildHandler.getGuilds().values().stream()
                .map(g -> getTag(g.getTag()))
                .toList();
    }

    public static List<String> getGuildsName() {
        return GuildHandler.getGuilds().values().stream()
                .map(Guilds::getName)
                .toList();
    }

    public static List<String> getGuildsId() {
        return GuildHandler.getGuilds().values().stream()
                .map(Guilds::getId)
                .toList();
    }


    public static void removeGuildsAlliesAndRivalsOnDelete(Guilds g) {
        GuildHandler.getGuilds().values().forEach(g2 -> {
            if (g2.isAlly(g) || g2.isRival(g)) {
                g2.removeAlly(g);
                g2.removeRival(g);
                g.removeAlly(g2);
                g.removeRival(g2);
            }
        });
        GuildHandler.saveGuildas();
    }



    public static String encodeItem(ItemStack item) {
        if (item == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(item);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack decodeItem(String encoded) {
        if (encoded == null) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            return (ItemStack) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ChatListener implements Listener {
        private final Player player;
        private final ChatInputCallback callback;

        public ChatListener(Player player, ChatInputCallback callback) {
            this.player = player;
            this.callback = callback;
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(player)) return;

            Bukkit.getScheduler().runTask(MineSkyGuildas.getInstance(), () -> {
                String msg = e.getMessage();

                if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("cancelar") ||
                        msg.equalsIgnoreCase("close") || msg.equalsIgnoreCase("sair")) {
                    callback.onCancel();
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    e.getPlayer().sendMessage(Utils.c("&cCancelando e retornando ao menu..."));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    return;
                }

                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                callback.onInput(Utils.c(msg));
            });
        }
    }

    public static String getTimeNow() {
        return java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"))
                .toLocalTime()
                .withNano(0)
                .toString();
    }

    public static String formatTime(long millis) {
        if (millis <= 0) return "0s";

        long totalSeconds = millis / 1000;
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String getRoleTagColor(GuildRoles role) {
            switch (role) {
                case LEADER:
                    return "&4♦";
                case SUB_LEADER:
                    return "&c♦";
                case CAPTAIN:
                    return "&6♦";
                case RECRUITER:
                    return "&e♦";
                case LOYAL:
                    return "&a♦";
                case MEMBER:
                    return "&9♦";
                case RECRUIT:
                    return "&8♦";
                default:
                    return "";
            }
        }

        public static String getGuildTagWithRole(UUID playerId) {
            Guilds guild = GuildHandler.getGuildByPlayer(playerId);
            GuildRoles role = guild.getRole(playerId);

            return " " + guild.getTag() + getRoleTagColor(role);
        }
}
