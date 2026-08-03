package net.mineskyguildas.war;

import net.mineskyguildas.data.Guilds;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WarSession {
    private final UUID warId;
    private final Guilds guild1;
    private final Guilds guild2;

    private WarState state;
    private LocalDateTime inviteTime;
    private LocalDateTime declarationTime;
    private LocalDateTime startTime;

    private int leader1OfflineTimeLeft = 600;
    private int leader2OfflineTimeLeft = 600;

    private final Set<String> guild1Supporters = new HashSet<>();
    private final Set<String> guild2Supporters = new HashSet<>();

    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
    private final Map<UUID, Integer> entitiesKilled = new HashMap<>();

    private long lastCountdownBroadcastMinute = -1;

    public WarSession(Guilds guild1, Guilds guild2) {
        this.warId = UUID.randomUUID();
        this.guild1 = guild1;
        this.guild2 = guild2;
        this.state = WarState.PENDING;
        this.inviteTime = LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
    }

    public WarSession(UUID warId, Guilds guild1, Guilds guild2, WarState state,
                      LocalDateTime inviteTime, LocalDateTime declarationTime, LocalDateTime startTime,
                      int leader1OfflineTimeLeft, int leader2OfflineTimeLeft) {
        this.warId = warId;
        this.guild1 = guild1;
        this.guild2 = guild2;
        this.state = state;
        this.inviteTime = inviteTime;
        this.declarationTime = declarationTime;
        this.startTime = startTime;
        this.leader1OfflineTimeLeft = leader1OfflineTimeLeft;
        this.leader2OfflineTimeLeft = leader2OfflineTimeLeft;
    }

    public UUID getWarId() { return warId; }
    public Guilds getGuild1() { return guild1; }
    public Guilds getGuild2() { return guild2; }
    public WarState getState() { return state; }
    public void setState(WarState state) { this.state = state; }
    public LocalDateTime getInviteTime() { return inviteTime; }
    public void setInviteTime(LocalDateTime inviteTime) { this.inviteTime = inviteTime; }
    public LocalDateTime getDeclarationTime() { return declarationTime; }
    public void setDeclarationTime(LocalDateTime declarationTime) { this.declarationTime = declarationTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public int getLeader1OfflineTimeLeft() { return leader1OfflineTimeLeft; }
    public void setLeader1OfflineTimeLeft(int time) { this.leader1OfflineTimeLeft = time; }
    public void decrementLeader1OfflineTime() { this.leader1OfflineTimeLeft--; }

    public int getLeader2OfflineTimeLeft() { return leader2OfflineTimeLeft; }
    public void setLeader2OfflineTimeLeft(int time) { this.leader2OfflineTimeLeft = time; }
    public void decrementLeader2OfflineTime() { this.leader2OfflineTimeLeft--; }

    public Set<String> getGuild1Supporters() { return guild1Supporters; }
    public Set<String> getGuild2Supporters() { return guild2Supporters; }

    public Map<UUID, Integer> getBlocksBroken() { return blocksBroken; }
    public Map<UUID, Integer> getEntitiesKilled() { return entitiesKilled; }

    public long getLastCountdownBroadcastMinute() { return lastCountdownBroadcastMinute; }
    public void setLastCountdownBroadcastMinute(long minute) { this.lastCountdownBroadcastMinute = minute; }
}