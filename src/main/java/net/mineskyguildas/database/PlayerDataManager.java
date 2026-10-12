package net.mineskyguildas.database;

import net.minesky.api.database.PlayerDatabase;
import net.minesky.api.database.UpdatedData;
import net.minesky.api.database.ValueType;
import net.minesky.core.databridge.callbacks.ErrorType;
import net.minesky.core.databridge.callbacks.FindOneCallback;
import net.minesky.core.databridge.callbacks.FindValueCallback;
import net.minesky.core.databridge.callbacks.SetOneCallback;
import org.bson.Document;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    public interface PlayerValueReturnCallback {
        void onQueryDone(double value);
    }

    public interface PlayerBooleanReturnCallback {
        void onQueryDone(boolean value);
    }

    public interface PlayerStatsCallback {
        void onQueryDone(int kills, int deaths, double kdr);
    }

    private static final Map<UUID, CachedStats> statsCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 3 * 60 * 1000;

    private record CachedStats(int kills, int deaths, double kdr, long timestamp) {
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    public void getPlayerStats(UUID uuid, PlayerStatsCallback callback) {
        CachedStats cached = statsCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            callback.onQueryDone(cached.kills(), cached.deaths(), cached.kdr());
            return;
        }

        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {
                callback.onQueryDone(0, 0, 0.0);
            }

            @Override
            public void onQueryDone(Document document) {
                if (document == null) {
                    callback.onQueryDone(0, 0, 0.0);
                    return;
                }
                int kills = parseIntSafe(document.get("kills"));
                int deaths = parseIntSafe(document.get("deaths"));
                double kdr = (deaths == 0) ? kills : ((double) kills / deaths);

                statsCache.put(uuid, new CachedStats(kills, deaths, kdr, System.currentTimeMillis()));
                callback.onQueryDone(kills, deaths, kdr);
            }
        });
    }

    public void setKills(UUID uuid, int kills) {
        statsCache.remove(uuid);
        PlayerDatabase.setPlayerData(uuid.toString(),
                new UpdatedData("kills", kills),
                new SetOneCallback() {
                    @Override
                    public void onSetDone() {}

                    @Override
                    public void onSetError(ErrorType errorType) {}
                });
    }

    public void setDeaths(UUID uuid, int deaths) {
        statsCache.remove(uuid);
        PlayerDatabase.setPlayerData(uuid.toString(),
                new UpdatedData("deaths", deaths),
                new SetOneCallback() {
                    @Override
                    public void onSetDone() {}

                    @Override
                    public void onSetError(ErrorType errorType) {}
                });
    }

    public void resetPlayerStats(UUID uuid) {
        setKills(uuid, 0);
        setDeaths(uuid, 0);
    }

    public void setSpy(UUID uuid, boolean enabled) {
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.BOOLEAN, "spy", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                PlayerDatabase.setPlayerData(uuid.toString(),
                        new UpdatedData("spy", enabled),
                        new SetOneCallback() {
                            @Override
                            public void onSetDone() {}
                            @Override
                            public void onSetError(ErrorType errorType) {}
                        });
            }

            @Override
            public void onQueryError(ErrorType errorType) {}
        });
    }

    public void setStatusCoord(UUID uuid, boolean p) {
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.BOOLEAN, "coords", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                PlayerDatabase.setPlayerData(uuid.toString(),
                        new UpdatedData("coords", p),
                        new SetOneCallback() {
                            @Override
                            public void onSetDone() {}
                            @Override
                            public void onSetError(ErrorType errorType) {}
                        });
            }

            @Override
            public void onQueryError(ErrorType errorType) {}
        });
    }

    public void addKill(UUID uuid, int kill) {
        statsCache.remove(uuid);
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.INTEGER, "kills", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                int currentKills = parseIntSafe(o);
                PlayerDatabase.setPlayerData(uuid.toString(),
                        new UpdatedData("kills", currentKills + kill),
                        new SetOneCallback() {
                            @Override
                            public void onSetDone() {}
                            @Override
                            public void onSetError(ErrorType errorType) {}
                        });
            }

            @Override
            public void onQueryError(ErrorType errorType) {}
        });
    }

    public void addDeath(UUID uuid, int death) {
        statsCache.remove(uuid);
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.INTEGER, "deaths", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                int currentDeaths = parseIntSafe(o);
                PlayerDatabase.setPlayerData(uuid.toString(),
                        new UpdatedData("deaths", currentDeaths + death),
                        new SetOneCallback() {
                            @Override
                            public void onSetDone() {}

                            @Override
                            public void onSetError(ErrorType errorType) {}
                        });
            }

            @Override
            public void onQueryError(ErrorType errorType) {}
        });
    }

    public void getKDR(UUID uuid, PlayerValueReturnCallback callback) {
        getPlayerStats(uuid, (kills, deaths, kdr) -> callback.onQueryDone(kdr));
    }

    public void getKills(UUID uuid, PlayerValueReturnCallback callback) {
        getPlayerStats(uuid, (kills, deaths, kdr) -> callback.onQueryDone(kills));
    }

    public void getDeaths(UUID uuid, PlayerValueReturnCallback callback) {
        getPlayerStats(uuid, (kills, deaths, kdr) -> callback.onQueryDone(deaths));
    }

    public void getSpy(UUID uuid, PlayerBooleanReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {
                callback.onQueryDone(true);
            }

            @Override
            public void onQueryDone(Document document) {
                if (document == null) {
                    callback.onQueryDone(true);
                    return;
                }
                boolean spy = document.getBoolean("spy", false);
                callback.onQueryDone(spy);
            }
        });
    }

    public void getStatusCoord(UUID uuid, PlayerBooleanReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {
                callback.onQueryDone(true);
            }

            @Override
            public void onQueryDone(Document document) {
                if (document == null) {
                    callback.onQueryDone(true);
                    return;
                }
                boolean coords = document.getBoolean("coords", true);
                callback.onQueryDone(coords);
            }
        });
    }

    private int parseIntSafe(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}