package net.minesky.utils;

import com.mongodb.client.model.ReplaceOptions;
import net.md_5.bungee.api.ChatColor;
import net.minesky.MineSkyGuildas;
import net.minesky.config.Config;
import net.minesky.data.Guilds;
import net.minesky.handlers.GuildHandler;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
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

    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
    public static List<String> getGuildsTags(Player p) {
        Guilds playerGuild = GuildHandler.getGuildByPlayer(p.getUniqueId());

        return GuildHandler.getGuilds().values().stream()
                .filter(g -> playerGuild == null || !g.getId().equals(playerGuild.getId()))
                .map(g -> getTag(g.getTag()))
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
}
