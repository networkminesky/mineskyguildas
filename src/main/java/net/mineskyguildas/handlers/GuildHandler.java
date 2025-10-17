package net.mineskyguildas.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.Notice;
import net.mineskyguildas.utils.Utils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GuildHandler {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();
    private static MongoCollection<Document> coll;
    private static final Map<String, Guilds> guildas = new HashMap<>();

    public GuildHandler() {
        coll = plugin.coll;
        loadGuildas();
    }

    private void loadGuildas() {
        for (Document doc : coll.find()) {
            Document meta = (Document) doc.get("meta");
            String name = meta != null ? meta.getString("name") : null;
            String tag = meta != null ? meta.getString("tag") : null;
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
                g.addMember(playerUUID, role, kills);
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
                            .append("kills", data.getKills());
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

    public static void saveGuildas() {
        Bukkit.getScheduler().runTaskAsynchronously(MineSkyGuildas.getInstance(), () -> {
            guildas.values().forEach(g -> {
                coll.replaceOne(
                        new Document("_id", g.getId()),
                        guildToDoc(g),
                        new ReplaceOptions().upsert(true)
                );
            });
        });
    }

    public static void createGuilda(String id, String name, String tag, Player lider) {
        Guilds guilda = new Guilds(id, name, tag, lider.getUniqueId(), 0, 0, 0);
        guildas.put(id, guilda);

        coll.replaceOne(
                new Document("_id", id),
                guildToDoc(guilda),
                new ReplaceOptions().upsert(true)
        );

        MineSkyGuildas.l.info(Utils.c("| Criando a guilda " + name + " (" + id + ")..."));
    }

    public static boolean doesGuildExist(String id) {
        return guildas.containsKey(id);
    }

    public static boolean doesGuildNameExist(String name) {
        return guildas.entrySet().stream()
                        .map(id -> id.getValue().getName())
                        .filter(Objects::nonNull)
                        .anyMatch(name::equalsIgnoreCase);
    }

    public static boolean doesGuildTagExist(String tag) {
        if (!Utils.isValidTag(tag) || coll == null) {
            return false;
        }
        tag = Utils.getTag(tag);
        String finalTag = tag;
        return guildas.entrySet().stream()
                .anyMatch(id -> finalTag.equalsIgnoreCase(Utils.getTag(id.getValue().getTag())));
    }

    public static boolean doesGuildTagExist(String tag, String ignoreGuildId) {
        if (!Utils.isValidTag(tag) || coll == null) {
            return false;
        }

        String finalTag = Utils.getTag(tag);
        if (finalTag == null) return false;

        return guildas.values().stream()
                .anyMatch(g -> (ignoreGuildId == null || !g.getId().equals(ignoreGuildId)) &&
                        Utils.getTag(g.getTag()).equalsIgnoreCase(finalTag));
    }

    public static String getGuildName(String id) {
        return getGuildByID(id).getName();
    }

    public static String getGuildTag(String id) {
        return getGuildByID(id).getTag();
    }

    public static int getGuildLevel(String id) {
        return getGuildByID(id).getLevel();
    }

    public static double getGuildXp(String id) {
        return getGuildByID(id).getXp();
    }

    public static Map<String, Guilds> getGuilds() {
        return guildas;
    }

    public static String getOnlineMembers(Guilds guild) {
        return guild.getMembers().keySet().stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count()
                + "/" + guild.getMembers().size();
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
        return guildas.values().stream()
                .filter(g -> Utils.getTag(g.getTag()).equalsIgnoreCase(tag))
                .findFirst()
                .orElse(null);
    }

    public static void addAlly(Guilds guild, Guilds ally, Player player) {
        guild.addAlly(ally.getId());
        ally.addAlly(guild.getId());
        broadcastGuildMessage(guild, Utils.c("&b🤝 &3Sua guilda selou uma aliança com &b" + ally.getName() + "&3!"));
        broadcastGuildMessage(ally, Utils.c("&3🤝 &b" + guild.getName() + " &3formou uma aliança com sua guilda!"));
        saveGuildas();
    }

    public static void removeAlly(Guilds guild, Guilds ally, Player player) {
        guild.removeAlly(ally);
        ally.removeAlly(guild);
        broadcastGuildMessage(ally, Utils.c("&4⚔ &cA aliança com &4" + guild.getName() + " &cfoi rompida."));
        broadcastGuildMessage(guild, Utils.c("&c⚔ &4" + player.getName() + " &crompeu a aliança com &4" + ally.getName() + "&c."));
        saveGuildas();
    }

    public static void addRival(Guilds guild, Guilds rival, Player player) {
        guild.addRival(rival.getId());
        rival.addRival(guild.getId());
        broadcastGuildMessage(rival, Utils.c("&4⚔ &cA guilda &f" + guild.getName() + " &cdeclarou rivalidade com a sua guilda!"));
        broadcastGuildMessage(guild, Utils.c("&c😡 &4" + player.getName() + " &cdeclarou rivalidade com a guilda &f" + rival.getName() + "&c."));
        saveGuildas();
    }

    public static void removeRival(Guilds guild, Guilds rival, Player player) {
        guild.removeRival(rival);
        rival.removeRival(guild);
        broadcastGuildMessage(guild, Utils.c("&2✌ &aA guilda &f" + rival.getName() + " &aremovou a rivalidade com a sua guilda."));
        broadcastGuildMessage(rival, Utils.c("&a✅ &2" + player.getName() + " &aremovou a rivalidade com a guilda &f" + guild.getName() + "&a."));
        saveGuildas();
    }

    public static void addMember(Player player, Guilds guild) {
        guild.addMember(player.getUniqueId(), GuildRoles.RECRUIT, 0);
        saveGuildas();
    }

    public static void removeMember(Player player, Guilds guild) {
        guild.removeMember(player.getUniqueId());
        saveGuildas();
    }

    public static void addKill(Player player, Guilds guild) {
        guild.getMemberData(player.getUniqueId()).addKill();
        saveGuildas();
    }

    public static void getMemberPromoteKills(Player player, Guilds guild) {
        int kills = guild.getKills(player.getUniqueId());
        GuildRoles currentRole = guild.getRole(player.getUniqueId());

        if (GuildRoles.isLeadership(currentRole)) return;
        if (kills >= 120 && currentRole != GuildRoles.LOYAL) {
            setRole(player, guild, GuildRoles.LOYAL);
            broadcastGuildMessage(guild, player.getName() + " Promovido para Leal");
        } else if (kills >= 40 && currentRole != GuildRoles.MEMBER) {
            setRole(player, guild, GuildRoles.MEMBER);
            broadcastGuildMessage(guild, player.getName() + " Promovido para Membro");
        }
    }

    public static void setRole(Player player, Guilds guild, GuildRoles role) {
        guild.getMemberData(player.getUniqueId()).setRole(role);
        saveGuildas();
    }

    public static void promotePlayer(Player player, Guilds guild) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        GuildRoles promoted = promoteRole(current);

        if (promoted != current) {
            guild.getMemberData(player.getUniqueId()).setRole(promoted);
            saveGuildas();
        }
    }

    public static void promotePlayer(Player player, Guilds guild, GuildRoles roles) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        if (roles != current) {
            guild.getMemberData(player.getUniqueId()).setRole(roles);
            saveGuildas();
        }
    }

    public static void demotePlayer(Player player, Guilds guild) {
        GuildRoles current = guild.getRole(player.getUniqueId());
        GuildRoles demoted = demoteRole(current);

        if (demoted != current) {
            guild.getMemberData(player.getUniqueId()).setRole(demoted);
            saveGuildas();
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
        saveGuildas();
    }

    public static boolean hasGuild(Player player) {
        for (Guilds guild : guildas.values()) {
            if (guild.getMembers().containsKey(player.getUniqueId())) {
                return true;
            }
        }
        return false;
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
        Set<UUID> recipients = new HashSet<>(guild.getMembers().keySet());

        if (allies) {
            guild.getAllies().stream()
                    .map(guildas::get)
                    .filter(Objects::nonNull)
                    .forEach(ally -> recipients.addAll(ally.getMembers().keySet()));
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
                .forEach(p -> p.sendMessage(spy));
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
                .forEach(p -> p.sendMessage(spy));
    }


    public static void addXpToGuild(UUID player, double xp) {
        Guilds guild = getGuildByPlayer(player);
        if (guild != null) {
            guild.addXP(xp);
            saveGuildas();
        }
    }

    public static void addNotice(Guilds guild, String message, Player sender) {
        guild.addNotice("&7[" + Utils.getTimeNow() + "] &b✉ &3" + sender.getName() + "&8: &f" + message);
        saveGuildas();
    }

    public static void addNotice(Guilds guild, String message) {
        guild.addNotice("&7[" + Utils.getTimeNow() + "] &f" + message);
        saveGuildas();
    }

    public static void removeNotice(Guilds guild, int index) {
        guild.removeNotice(index);
        saveGuildas();
    }

    public static void deleteGuild(String id) {
        if (!guildas.containsKey(id)) {
            MineSkyGuildas.l.warning(Utils.c("| Guilda com ID '" + id + "' não encontrada."));
            return;
        }

        Utils.removeGuildsAlliesAndRivalsOnDelete(getGuildByID(id));
        guildas.remove(id);
        coll.deleteOne(new Document("_id", id));
        MineSkyGuildas.l.info(Utils.c("| Guilda com ID '" + id + "' foi deletada com sucesso."));
    }

    public void close() { plugin.mm.close(); }
}
