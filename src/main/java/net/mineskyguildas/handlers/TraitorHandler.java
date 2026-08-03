package net.mineskyguildas.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.mineskyguildas.MineSkyGuildas;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TraitorHandler {
    private final MineSkyGuildas plugin;
    private final Map<UUID, String> globalTraitors = new ConcurrentHashMap<>();
    private final Map<String, Long> clanXpCooldowns = new ConcurrentHashMap<>();

    private final MongoCollection<Document> traitorsColl;
    private final MongoCollection<Document> cooldownsColl;

    public TraitorHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
        this.traitorsColl = plugin.mm.getGuildTraitors();
        this.cooldownsColl = plugin.mm.getGuildTraitorCooldowns();

        loadTraitorsFromDatabase();
        loadCooldownsFromDatabase();
    }

    private void loadTraitorsFromDatabase() {
        for (Document doc : traitorsColl.find()) {
            try {
                UUID uuid = UUID.fromString(doc.getString("_id"));
                String originalClanName = doc.getString("original_clan");
                globalTraitors.put(uuid, originalClanName);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar traidor do banco: " + e.getMessage());
            }
        }
    }

    private void loadCooldownsFromDatabase() {
        for (Document doc : cooldownsColl.find()) {
            try {
                String clanId = doc.getString("_id");
                long timestamp = doc.getLong("timestamp");
                clanXpCooldowns.put(clanId, timestamp);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar cooldown de traidor: " + e.getMessage());
            }
        }
    }

    public void addTraitor(UUID playerUuid, String originalClanName) {
        globalTraitors.put(playerUuid, originalClanName);

        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            Document doc = new Document("_id", playerUuid.toString())
                    .append("original_clan", originalClanName);
            traitorsColl.replaceOne(new Document("_id", playerUuid.toString()), doc, new ReplaceOptions().upsert(true));
        });
    }

    public void removeTraitor(UUID playerUuid) {
        globalTraitors.remove(playerUuid);

        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            traitorsColl.deleteOne(new Document("_id", playerUuid.toString()));
        });
    }

    public boolean isTraitor(UUID playerUuid) {
        return globalTraitors.containsKey(playerUuid);
    }

    public String getOriginalClanName(UUID playerUuid) {
        return globalTraitors.get(playerUuid);
    }

    public boolean canClanReceiveXP(String clanId) {
        if (!clanXpCooldowns.containsKey(clanId)) return true;
        long lastTime = clanXpCooldowns.get(clanId);
        return (System.currentTimeMillis() - lastTime) >= (24L * 60 * 60 * 1000);
    }

    public long getClanCooldownRemaining(String clanId) {
        if (!clanXpCooldowns.containsKey(clanId)) return 0;
        long elapsed = System.currentTimeMillis() - clanXpCooldowns.get(clanId);
        long remaining = (24L * 60 * 60 * 1000) - elapsed;
        return Math.max(0, remaining);
    }

    public void applyClanCooldown(String clanId) {
        long now = System.currentTimeMillis();
        clanXpCooldowns.put(clanId, now);

        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            Document doc = new Document("_id", clanId)
                    .append("timestamp", now);
            cooldownsColl.replaceOne(new Document("_id", clanId), doc, new ReplaceOptions().upsert(true));
        });
    }

    public Map<UUID, String> getGlobalTraitors() {
        return globalTraitors;
    }
}