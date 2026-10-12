package net.mineskyguildas.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.data.Notice;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.hooks.MineSkyVanishHook;
import net.mineskyguildas.utils.Utils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.*;

public class GuildHandler {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();
    private static MongoCollection<Document> coll;
    private static final Map<String, Guilds> guildas = new ConcurrentHashMap<>();

    private static final Set<String> dirtyGuilds = ConcurrentHashMap.newKeySet();

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "MineSkyGuildas-DB-Thread");
        thread.setDaemon(true);
        return thread;
    });

    public GuildHandler() {
        coll = plugin.coll;
        loadGuildas();
    }

    private void loadGuildas() {
        for (Document doc : coll.find()) {

            if (doc.getString("_id").startsWith("cooldown_")) continue;

            Document meta = (Document) doc.get("meta");
            String name = meta != null ? meta.getString("name") : null;
            String tag = meta != null ? meta.getString("tag") : null;

            if (tag != null) {
                tag = Utils.toSmallCaps(tag);
            }

            String description = meta != null ? meta.getString("description") : null;
            int level = meta != null ? meta.getInteger("level") : 0;
            double xp = meta != null ? meta.getDouble("xp") : 0;
            int balance = meta != null ? meta.getInteger("balance") : 0;
            boolean friendly_fire = meta != null ? meta.getBoolean("friendly-fire") : false;
            String base = meta != null ? meta.getString("base") : null;

            String id = doc.getString("_id");
            Guilds g = new Guilds(id,
                    name,
                    tag,
                    UUID.fromString(doc.getString("leader")),
                    level,
                    xp,
                    balance);

            doc.getList("members", Document.class).forEach(m -> {
                UUID playerUUID = UUID.fromString(m.getString("uuid"));
                GuildRoles role = GuildRoles.valueOf(m.getString("role"));
                int kills = m.containsKey("kills") ? m.getInteger("kills") : 0;
                long joinedAt = m.containsKey("joinedAt") ? m.getLong("joinedAt") : System.currentTimeMillis();
                g.addMember(playerUUID, role, kills, joinedAt);
            });

            assert meta != null;
            g.setBanner(meta.containsKey("banner-item") ? Utils.decodeItem(meta.getString("banner-item")) : null);
            g.setFriendlyFire(friendly_fire);
            g.setBase(Utils.deserializeLocation(base));
            g.setDescription(description);
            g.getRivals().addAll(doc.getList("rivals", String.class));
            g.getAllies().addAll(doc.getList("allies", String.class));

            if (doc.containsKey("notice-board")) {
                List<Document> noticesDocs = (List<Document>) doc.get("notice-board");
                List<Notice> notices = new ArrayList<>();
                for (Document noticeDoc : noticesDocs) {
                    String message = noticeDoc.getString("message");
                    long timestamp = noticeDoc.getLong("timestamp");
                    notices.add(new Notice(message, timestamp));
                }
                g.setNoticeBoard(notices);
            }

            guildas.put(id, g);
        }
    }

    private static Document guildToDoc(Guilds g) {
        List<Document> members = g.getMembers().entrySet().stream()
                .map(e -> {
                    UUID uuid = e.getKey();
                    MemberData data = e.getValue();
                    return new Document("uuid", uuid.toString())
                            .append("role", data.getRole().name())
                            .append("kills", data.getKills())
                            .append("joinedAt", data.getJoinedAt());
                })
                .toList();

        List<Document> noticesDocs = g.getNoticeBoard().stream()
                .map(n -> new Document("message", n.getMessage()).append("timestamp", n.getTimestamp()))
                .toList();

        return new Document("_id", g.getId())
                .append("leader", g.getLeader().toString())
                .append("members", members)
                .append("rivals", g.getRivals())
                .append("allies", g.getAllies())
                .append("notice-board", noticesDocs)
                .append("meta",
                        new Document("name", g.getName())
                                .append("tag", g.getTag())
                                .append("description", g.getDescription())
                                .append("level", g.getLevel())
                                .append("xp", g.getXp())
                                .append("balance", g.getBalance())
                                .append("friendly-fire", g.getFriendlyFire())
                                .append("base", Utils.serializeLocation(g.getBase()))
                                .append("banner-item", g.getBanner() != null ? Utils.encodeItem(g.getBanner()) : null));
    }

    public static void saveGuild(Guilds g) {
        if (g == null) return;
        dirtyGuilds.add(g.getId());
        flushDirty();
    }

    public static void saveGuildas() {
        dirtyGuilds.addAll(guildas.keySet());
        flushDirty();
    }

    private static void flushDirty() {
        if (coll == null) return;
        DB_EXECUTOR.execute(() -> {
            if (dirtyGuilds.isEmpty()) return;

            Set<String> toSave = new HashSet<>(dirtyGuilds);
            dirtyGuilds.removeAll(toSave);

            List<WriteModel<Document>> operations = new ArrayList<>();
            for (String id : toSave) {
                Guilds g = guildas.get(id);
                if (g != null) {
                    operations.add(new ReplaceOneModel<>(
                            new Document("_id", g.getId()),
                            guildToDoc(g),
                            new ReplaceOptions().upsert(true)
                    ));
                }
            }

            if (!operations.isEmpty()) {
                try {
                    coll.bulkWrite(operations, new BulkWriteOptions().ordered(false));
                } catch (Exception e) {
                    MineSkyGuildas.l.severe("Erro ao salvar guildas no MongoDB: " + e.getMessage());
                }
            }
        });
    }

    public static void saveAllSync() {
        if (coll == null) return;
        dirtyGuilds.addAll(guildas.keySet());
        if (dirtyGuilds.isEmpty()) return;

        List<WriteModel<Document>> operations = new ArrayList<>();
        for (String id : dirtyGuilds) {
            Guilds g = guildas.get(id);
            if (g != null) {
                operations.add(new ReplaceOneModel<>(
                        new Document("_id", g.getId()),
                        guildToDoc(g),
                        new ReplaceOptions().upsert(true)
                ));
            }
        }
        dirtyGuilds.clear();

        if (!operations.isEmpty()) {
            try {
                coll.bulkWrite(operations, new BulkWriteOptions().ordered(false));
            } catch (Exception e) {
                MineSkyGuildas.l.severe("Erro no salvamento final das guildas: " + e.getMessage());
            }
        }
    }

    public static void createGuilda(String id, String name, String tag, Player lider) {
        Guilds guilda = new Guilds(id, name, tag, lider.getUniqueId(), 0, 0, 0);
        guildas.put(id, guilda);

        saveGuild(guilda);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updatePlayer(lider);
        }

        MineSkyGuildas.l.info(Utils.c("| Criando a guilda " + name + " (" + id + ")..."));
    }

    public static boolean doesGuildExist(String id) {
        return guildas.containsKey(id);
    }

    public static boolean doesGuildNameExist(String name) {
        if (MineSkyGuildas.getInstance().getWarHandler().isNameUnderCooldown(name)) {
            return true;
        }
        return guildas.entrySet().stream()
                .map(id -> id.getValue().getName())
                .filter(Objects::nonNull)
                .anyMatch(name::equalsIgnoreCase);
    }

    public static boolean doesGuildTagExist(String tag) {
        if (!Utils.isValidTag(tag) || coll == null) {
            return false;
        }
        if (MineSkyGuildas.getInstance().getWarHandler().isTagUnderCooldown(tag)) {
            return true;
        }

        String normalized = Utils.normalizeTag(tag);
        return guildas.values().stream()
                .filter(g -> g.getTag() != null)
                .anyMatch(g -> Utils.normalizeTag(g.getTag()).equalsIgnoreCase(normalized));
    }

    public static boolean doesGuildTagExist(String tag, String ignoreGuildId) {
        if (!Utils.isValidTag(tag) || coll == null) {
            return false;
        }

        String normalized = Utils.normalizeTag(tag);
        if (normalized.isEmpty()) return false;

        return guildas.values().stream()
                .filter(g -> g.getTag() != null)
                .anyMatch(g -> (ignoreGuildId == null || !g.getId().equals(ignoreGuildId)) &&
                        Utils.normalizeTag(g.getTag()).equalsIgnoreCase(normalized));
    }

    public static String getGuildName(String id) {
        Guilds g = getGuildByID(id);
        return g != null ? g.getName() : "";
    }

    public static String getGuildTag(String id) {
        Guilds g = getGuildByID(id);
        return g != null ? g.getTag() : "";
    }

    public static int getGuildLevel(String id) {
        Guilds g = getGuildByID(id);
        return g != null ? g.getLevel() : 0;
    }

    public static double getGuildXp(String id) {
        Guilds g = getGuildByID(id);
        return g != null ? g.getXp() : 0.0;
    }

    public static Map<String, Guilds> getGuilds() {
        return guildas;
    }

    public static String getOnlineMembers(Guilds guild) {
        return guild.getMembers().keySet().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .filter(uuid -> !MineSkyVanishHook.isPlayerVanished(uuid))
                .count() + "/" + guild.getMembers().size();
    }

    public static List<String> getAlliesTags(Guilds guild) {
        return guild.getAllies().stream()
                .map(GuildHandler::getGuildByID)
                .filter(Objects::nonNull)
                .map(Guilds::getTag)
                .toList();
    }

    public static List<String> getRivalsTags(Guilds guild) {
        return guild.getRivals().stream()
                .map(GuildHandler::getGuildByID)
                .filter(Objects::nonNull)
                .map(Guilds::getTag)
                .toList();
    }

    public static Guilds getGuildByPlayer(UUID player) {
        for (Guilds guild : guildas.values()) {
            if (guild.getMembers().containsKey(player)) {
                return guild;
            }
        }
        return null;
    }

    public static Guilds getGuildByPlayer(Player player) {
        return getGuildByPlayer(player.getUniqueId());
    }

    public static Guilds getGuildByID(String id) {
        return guildas.get(id);
    }

    public static Guilds getGuildByTag(String tag) {
        if (tag == null || tag.isEmpty()) return null;

        String normalizedTarget = Utils.normalizeTag(tag);

        return guildas.values().stream()
                .filter(g -> g.getTag() != null && Utils.normalizeTag(g.getTag()).equalsIgnoreCase(normalizedTarget))
                .findFirst()
                .orElse(null);
    }

    public static void addAlly(Guilds guild, Guilds ally, Player player) {
        guild.addAlly(ally.getId());
        ally.addAlly(guild.getId());
        broadcastGuildMessage(guild, Utils.c("&b🤝 &3Seu clã selou uma aliança com &b" + ally.getName() + "&3!"));
        broadcastGuildMessage(ally, Utils.c("&3🤝 &b" + guild.getName() + " &3formou uma aliança com seu clã!"));

        saveGuild(guild);
        saveGuild(ally);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updateBetweenGuilds(guild, ally);
        }
    }

    public static void removeAlly(Guilds guild, Guilds ally, Player player) {
        guild.removeAlly(ally);
        ally.removeAlly(guild);
        broadcastGuildMessage(ally, Utils.c("&4⚔ &cA aliança com &4" + guild.getName() + " &cfoi rompida."));
        broadcastGuildMessage(guild, Utils.c("&c⚔ &4" + player.getName() + " &crompeu a aliança com &4" + ally.getName() + "&c."));

        saveGuild(guild);
        saveGuild(ally);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updateBetweenGuilds(guild, ally);
        }
    }

    public static void addRival(Guilds guild, Guilds rival, Player player) {
        guild.addRival(rival.getId());
        rival.addRival(guild.getId());
        broadcastGuildMessage(rival, Utils.c("&4⚔ &cO clã &f" + guild.getName() + " &cdeclarou rivalidade com a seu clã!"));
        broadcastGuildMessage(guild, Utils.c("&c😡 &4" + player.getName() + " &cdeclarou rivalidade com o clã &f" + rival.getName() + "&c."));
        MineSkyGuildas.getInstance().getWarHandler().recordRivalry(guild.getId(), rival.getId());

        saveGuild(guild);
        saveGuild(rival);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updateBetweenGuilds(guild, rival);
        }
    }

    public static void removeRival(Guilds guild, Guilds rival, Player player) {
        guild.removeRival(rival);
        rival.removeRival(guild);
        broadcastGuildMessage(guild, Utils.c("&2✌ &aO clã &f" + rival.getName() + " &aremoveu a rivalidade com a seu clã."));
        broadcastGuildMessage(rival, Utils.c("&a✅ &2" + player.getName() + " &aremoveu a rivalidade com o clã &f" + guild.getName() + "&a."));
        MineSkyGuildas.getInstance().getWarHandler().removeRivalryRecord(guild.getId(), rival.getId());

        saveGuild(guild);
        saveGuild(rival);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updateBetweenGuilds(guild, rival);
        }
    }

    public static void addMember(Player player, Guilds guild) {
        guild.addMember(player.getUniqueId(), GuildRoles.RECRUIT, 0, System.currentTimeMillis());
        saveGuild(guild);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().updatePlayer(player);
            MineSkyGuildas.getInstance().getLocator().updateGuild(guild);
        }
    }

    public static void removeMember(Player player, Guilds guild) {
        guild.removeMember(player.getUniqueId());
        saveGuild(guild);

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().onPlayerLeaveGuild(player);
            MineSkyGuildas.getInstance().getLocator().updateGuild(guild);
        }
    }

    public static void removeMember(UUID uuid, Guilds guild) {
        guild.removeMember(uuid);
        saveGuild(guild);

        Player target = Bukkit.getPlayer(uuid);
        if (MineSkyGuildas.getInstance().getLocator() != null) {
            if (target != null) {
                MineSkyGuildas.getInstance().getLocator().onPlayerLeaveGuild(target);
            }
            MineSkyGuildas.getInstance().getLocator().updateGuild(guild);
        }
    }

    public static void addKill(Player player, Guilds guild) {
        guild.getMemberData(player.getUniqueId()).addKill();
        saveGuild(guild);
    }

    public static void getMemberPromoteKills(Player player, Guilds guild) {
        int kills = guild.getKills(player.getUniqueId());
        GuildRoles currentRole = guild.getRole(player.getUniqueId());

        if (GuildRoles.isLeadershipAndRecruiter(currentRole)) return;
        if (kills >= 120 && currentRole != GuildRoles.LOYAL) {
            setRole(player, guild, GuildRoles.LOYAL);
            broadcastGuildMessage(guild, "&3➕ &b" + player.getName() + " &3atingiu &b120 KILLS &3e foi promovido a &bLEAL");
        } else if (kills >= 40 && currentRole != GuildRoles.MEMBER) {
            setRole(player, guild, GuildRoles.MEMBER);
            broadcastGuildMessage(guild, "&3➕ &b" + player.getName() + " &3atingiu &b40 KILLS &3e foi promovido a &bMEMBRO");
        }
    }

    public static void setRole(Player player, Guilds guild, GuildRoles role) {
        guild.getMemberData(player.getUniqueId()).setRole(role);
        saveGuild(guild);
    }

    public static void promotePlayer(Player player, Guilds guild) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        GuildRoles promoted = promoteRole(current);

        if (promoted != current) {
            guild.getMemberData(player.getUniqueId()).setRole(promoted);
            saveGuild(guild);
        }
    }

    public static void promotePlayer(Player player, Guilds guild, GuildRoles roles) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        if (roles != current) {
            guild.getMemberData(player.getUniqueId()).setRole(roles);
            saveGuild(guild);
        }
    }

    public static void demotePlayer(Player player, Guilds guild) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        GuildRoles demoted = demoteRole(current);

        if (demoted != current) {
            guild.getMemberData(player.getUniqueId()).setRole(demoted);
            saveGuild(guild);
        }
    }

    public static GuildRoles promoteRole(GuildRoles current) {
        return switch (current) {
            case RECRUIT -> GuildRoles.RECRUIT;
            case MEMBER -> GuildRoles.MEMBER;
            case LOYAL -> GuildRoles.LOYAL;
            case RECRUITER -> GuildRoles.CAPTAIN;
            case CAPTAIN, SUB_LEADER -> GuildRoles.SUB_LEADER;
            case LEADER -> GuildRoles.LEADER;
        };
    }

    public static GuildRoles demoteRole(GuildRoles current) {
        return switch (current) {
            case LEADER -> GuildRoles.LEADER;
            case SUB_LEADER -> GuildRoles.CAPTAIN;
            case CAPTAIN -> GuildRoles.RECRUITER;
            case RECRUITER, LOYAL -> GuildRoles.LOYAL;
            case MEMBER ->  GuildRoles.MEMBER;
            case RECRUIT -> GuildRoles.RECRUIT;
        };
    }

    public static void setBanner(ItemStack item, Guilds guild) {
        guild.setBanner(item);
        saveGuild(guild);
    }

    public static boolean hasGuild(Player player) {
        if (player == null) return false;

        return guildas.values().stream()
                .anyMatch(guild -> guild.getMembers().containsKey(player.getUniqueId()));
    }

    public static void broadcastGuildMessage(Guilds guild, String message) {
        addNotice(guild, message);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> guild.getMembers().containsKey(p.getUniqueId()))
                .forEach(p -> p.sendMessage(Utils.c("&8* " + message)));
    }

    public static void broadcastGuildMessageNoNotice(Guilds guild, String message) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> guild.getMembers().containsKey(p.getUniqueId()))
                .forEach(p -> p.sendMessage(Utils.c("&8* " + message)));
    }

    public static void broadcastGuildChat(Player player, Guilds guild, String message, boolean allies) {
        Set<UUID> recipients = new HashSet<>();

        if (allies) {
            guild.getMembers().keySet().stream()
                    .filter(uuid -> guild.getRole(uuid) != GuildRoles.RECRUIT)
                    .forEach(recipients::add);

            guild.getAllies().stream()
                    .map(guildas::get)
                    .filter(Objects::nonNull)
                    .forEach(ally -> ally.getMembers().keySet().stream()
                            .filter(uuid -> ally.getRole(uuid) != GuildRoles.RECRUIT)
                            .forEach(recipients::add)
                    );
        } else {
            recipients.addAll(guild.getMembers().keySet());
        }

        String name = player.getCustomName() != null ? player.getCustomName() : player.getName();
        String role = GuildRoles.getLabelRole(guild.getRole(player.getUniqueId()));
        String tag = guild.getTag();

        String prefix = Utils.c((allies ? "&3[Aliados] " : "") +
                "&#45818e[&f" + tag + "&#45818e] &b" + name +
                " &3(" + role.toUpperCase() + ")&8: &#b9f7f7" + message);

        String spy = Utils.c("&3[Spy] " +
                (allies ? "&7[Aliados] " : "") +
                "&8[&f" + tag + "&8] &7" + name +
                " &8(" + role.toUpperCase() + ")&7: &f" + message);

        recipients.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .forEach(p -> p.sendMessage(prefix));

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !recipients.contains(p.getUniqueId()))
                .filter(p -> p.hasPermission("mineskyguildas.spy"))
                .forEach(p -> {
                    MineSkyGuildas.getInstance().getPlayerData().getSpy(p.getUniqueId(), spyAtivado -> {
                        if (spyAtivado) {
                            p.getScheduler().run(MineSkyGuildas.getInstance(), task -> {
                                p.sendMessage(spy);
                            }, null);
                        }
                    });
                });
        Bukkit.getConsoleSender().sendMessage("[SPY] " + (allies ? "[ALIADOS]" : "") + "[" + Utils.getTag(tag) + "] " + name + " (" + role.toUpperCase() + "): " + message);
    }

    public static void broadcastLeaderChat(Player sender, Guilds guilds, String message) {
        String name = sender.getCustomName() != null ? sender.getCustomName() : sender.getName();
        String role = GuildRoles.getLabelRole(guilds.getRole(sender.getUniqueId()));
        String tag = guilds.getTag();

        String prefix = Utils.c("&#ff5555[Líderes] &#45818e[&f" + tag + "&#45818e] &b" + name +
                " &3(" + role.toUpperCase() + ")&8: &#b9f7f7" + message);

        String spy = Utils.c("&3[Spy] &7[Líderes] &8[&f" + tag + "&8] &7" + name +
                " &8(" + role.toUpperCase() + ")&7: &f" + message);

        guilds.getMembers().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .filter(p -> GuildRoles.isLeadership(guilds.getRole(p.getUniqueId())))
                .forEach(p -> p.sendMessage(prefix));

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !GuildRoles.isLeadership(guilds.getRole(p.getUniqueId())))
                .filter(p -> p.hasPermission("mineskyguildas.spy"))
                .forEach(p -> {
                    MineSkyGuildas.getInstance().getPlayerData().getSpy(p.getUniqueId(), spyAtivado -> {
                        if (spyAtivado) {
                            p.getScheduler().run(MineSkyGuildas.getInstance(), task -> {
                                p.sendMessage(spy);
                            }, null);
                        }
                    });
                });
        Bukkit.getConsoleSender().sendMessage("[SPY] [LÍDERES] [" + Utils.getTag(tag) + "] " + name + " (" + role.toUpperCase() + "): " + message);
    }

    public static void addXpToGuild(UUID player, double xp) {
        Guilds guild = getGuildByPlayer(player);
        if (guild != null) {
            guild.addXP(xp);
            saveGuild(guild);
        }
    }

    public static void addNotice(Guilds guild, String message, Player sender) {
        guild.addNotice("&7[" + Utils.getTimeNow() + "] &b✉ &3" + sender.getName() + "&8: &f" + message);
        saveGuild(guild);
    }

    public static void addNotice(Guilds guild, String message) {
        guild.addNotice("&7[" + Utils.getTimeNow() + "] &f" + message);
        saveGuild(guild);
    }

    public static void removeNotice(Guilds guild, int index) {
        guild.removeNotice(index);
        saveGuild(guild);
    }

    public static void deleteGuild(String id) {
        Guilds guild = getGuildByID(id);
        if (guild == null) {
            MineSkyGuildas.l.warning(Utils.c("| Guilda com ID '" + id + "' não encontrada."));
            return;
        }

        if (MineSkyGuildas.getInstance().getLocator() != null) {
            MineSkyGuildas.getInstance().getLocator().onGuildDeleted(guild);
        }

        Utils.removeGuildsAlliesAndRivalsOnDelete(guild);
        MineSkyGuildas.getInstance().getWarHandler().removeAllRivalriesForGuild(id);

        guildas.remove(id);
        dirtyGuilds.remove(id);

        DB_EXECUTOR.execute(() -> {
            try {
                coll.deleteOne(new Document("_id", id));
            } catch (Exception e) {
                MineSkyGuildas.l.severe("Erro ao deletar guilda no MongoDB: " + e.getMessage());
            }
        });

        MineSkyGuildas.l.info(Utils.c("| Guilda com ID '" + id + "' foi deletada com sucesso."));
    }

    public void close() {
        saveAllSync();

        DB_EXECUTOR.shutdown();
        try {
            if (!DB_EXECUTOR.awaitTermination(2, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            DB_EXECUTOR.shutdownNow();
        }

        if (plugin.mm != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    plugin.mm.close();
                } catch (Exception ignored) {}
            });
        }
    }
}