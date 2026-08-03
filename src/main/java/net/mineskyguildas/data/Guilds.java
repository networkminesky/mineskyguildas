package net.mineskyguildas.data;

import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Guilds {
    private static final int MAX_LEVEL = 6;

    private final String id;
    private final String name;
    private String description;
    private String tag;
    private UUID leader;
    private int level;
    private double xp;
    private int balance;
    private boolean friendly_fire;
    private Location base;
    private List<String> rivals;
    private List<String> allies;
    private final Map<UUID, MemberData> members;
    private final List<Notice> noticeBoard;
    private ItemStack banner;

    public Guilds(String id, String name, String tag, UUID leader, int level, double xp, int balance) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.level = level;
        this.xp = xp;
        this.balance = balance;
        this.rivals = new ArrayList<>();
        this.allies = new ArrayList<>();
        this.members = new HashMap<>();
        this.noticeBoard = new ArrayList<>();

        this.members.put(leader, new MemberData(this, GuildRoles.LEADER, 0, System.currentTimeMillis()));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }

    public UUID getLeader() {
        return leader;
    }

    public int getLevel() {
        return level;
    }

    public double getXp() {
        return xp;
    }

    public int getBalance() {
        return balance;
    }

    public boolean getFriendlyFire() {
        return friendly_fire;
    }

    public Location getBase() {
        return base;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<UUID, MemberData> getMembers() {
        return members;
    }

    public void addMember(UUID player, GuildRoles role, int kills, long joinedAt) {
        members.put(player, new MemberData(this, role, kills, joinedAt));
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public GuildRoles getRole(UUID uuid) {
        MemberData member = members.get(uuid);
        return member != null ? member.getRole() : GuildRoles.RECRUIT;
    }

    public int getKills(UUID uuid) {
        MemberData member = members.get(uuid);
        return member != null ? member.getKills() : 0;
    }

    public MemberData getMemberData(UUID uuid) {
        return members.get(uuid);
    }

    public void addRival(String id) {
        if (!rivals.contains(id)) {
            rivals.add(id);
        }
    }

    public void removeRival(Guilds guild) {
        rivals.remove(guild.getId());
    }

    public boolean isRival(Guilds guild) {
        return rivals.contains(guild.getId());
    }

    public List<String> getRivals() {
        return rivals != null ? rivals : new ArrayList<>();
    }

    public void addAlly(String id) {
        if (!allies.contains(id)) {
            allies.add(id);
        }
    }

    public void removeAlly(Guilds guild) {
        allies.remove(guild.getId());
    }

    public boolean isAlly(Guilds guild) {
        return allies.contains(guild.getId());
    }

    public List<String> getAllies() {
        return allies != null ? allies : new ArrayList<>();
    }

    public int getMemberLimit() {
        return level == 0 ? 1 : level * 4;
    }

    public void addXP(double amount) {
        if (level >= MAX_LEVEL) return;
        this.xp += amount;
        checkLevelUp();
    }

    public void removeXP(double amount) {
        this.xp -= amount;
        checkLevelDown();
    }

    private void checkLevelDown() {
        while (this.xp < 0 && this.level > 0) {
            this.level--;
            this.xp += xpRequiredForNextLevel();
            GuildHandler.broadcastGuildMessage(GuildHandler.getGuildByID(getId()),
                    Utils.c("&c\uD83D\uDC09 &4O clã perdeu XP demais e caiu para o Nível &c&l" + level + "&4!"));
        }

        if (this.level == 0 && this.xp < 0) {
            this.xp = 0;
        }
    }

    private void checkLevelUp() {
        while (level < MAX_LEVEL && this.xp >= xpRequiredForNextLevel()) {
            this.xp -= xpRequiredForNextLevel();
            this.level++;
            GuildHandler.broadcastGuildMessage(GuildHandler.getGuildByID(getId()), "&b\uD83D\uDC09 &3Nível &b&l" + level + "&3 conquistado&b!");
        }

        if (level >= MAX_LEVEL) {
            this.xp = 0;
        }
    }

    public double xpRequiredForNextLevel() {
        switch (level) {
            case 0: return 5000;
            case 1: return 15000;
            case 2: return 35000;
            case 3: return 75000;
            case 4: return 150000;
            case 5: return 300000;
            default: return 0;
        }
    }


    public ItemStack getBanner() {
        return banner;
    }

    public void setBanner(ItemStack banner) {
        this.banner = banner;
    }

    public void setFriendlyFire(boolean friendly_fire) {
        this.friendly_fire = friendly_fire;
    }

    public void setBase(Location base) {
        this.base = base;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Notice> getNoticeBoard() {
        return noticeBoard;
    }

    public void setNoticeBoard(List<Notice> noticeBoard) {
        this.noticeBoard.clear();
        this.noticeBoard.addAll(noticeBoard);
    }

    public void addNotice(String message) {
        if (noticeBoard.size() >= 10) {
            int removeCount = noticeBoard.size() - 9;
            for (int i = 0; i < removeCount; i++) {
                noticeBoard.remove(0);
            }
        }
        noticeBoard.add(new Notice(message));
    }

    public void removeNotice(int index) {
        if (index >= 0 && index < noticeBoard.size()) {
            noticeBoard.remove(index);
        }
    }

    public void removeOldNotices() {
        long threeDaysMillis = 3L * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();

        noticeBoard.removeIf(notice -> (now - notice.getTimestamp()) > threeDaysMillis);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXP(double xp) {
        this.xp = xp;
    }

    public void setLeader(UUID uuid) {
        this.leader = uuid;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void deposit(int amount) {
        this.balance += amount;
    }

    public boolean withdraw(int amount) {
        if(this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
}
