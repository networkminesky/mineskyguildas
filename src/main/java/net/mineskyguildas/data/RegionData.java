package net.mineskyguildas.data;

// Classe para armazenar dados de uma região que pode ser dominada (para Drawn)
public class RegionData {
    private final String regionName; // Nome da região do WorldGuard (ex: r_test)
    private String ownerGuildId; // ID da guilda que possui a região
    private String currentAttackerId; // ID da guilda que está atacando a região (se houver guerra)
    private long warStartTime; // Timestamp do início da guerra (em millis)
    private boolean inWar; // Indica se a região está em guerra

    public RegionData(String regionName) {
        this(regionName, null, null, 0L, false);
    }

    public RegionData(String regionName, String ownerGuildId, String currentAttackerId, long warStartTime, boolean inWar) {
        this.regionName = regionName;
        this.ownerGuildId = ownerGuildId;
        this.currentAttackerId = currentAttackerId;
        this.warStartTime = warStartTime;
        this.inWar = inWar;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getOwnerGuildId() {
        return ownerGuildId;
    }

    public void setOwnerGuildId(String ownerGuildId) {
        this.ownerGuildId = ownerGuildId;
    }

    public String getCurrentAttackerId() {
        return currentAttackerId;
    }

    public void setCurrentAttackerId(String currentAttackerId) {
        this.currentAttackerId = currentAttackerId;
    }

    public long getWarStartTime() {
        return warStartTime;
    }

    public void setWarStartTime(long warStartTime) {
        this.warStartTime = warStartTime;
    }

    public boolean isInWar() {
        return inWar;
    }

    public void setInWar(boolean inWar) {
        this.inWar = inWar;
    }
}
