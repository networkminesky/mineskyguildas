package net.mineskyguildas.database;

import net.minesky.api.database.PlayerDatabase;
import net.minesky.api.database.UpdatedData;
import net.minesky.api.database.ValueType;
import net.minesky.core.databridge.callbacks.ErrorType;
import net.minesky.core.databridge.callbacks.FindOneCallback;
import net.minesky.core.databridge.callbacks.FindValueCallback;
import net.minesky.core.databridge.callbacks.SetOneCallback;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayerDataManager {

    public interface PlayerValueReturnCallback {
        void onQueryDone(double value);
    }

    public interface PlayerBooleanReturnCallback {
        void onQueryDone(boolean value);
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
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
                int kills = parseIntSafe(document.get("kills"));
                int deaths = parseIntSafe(document.get("deaths"));

                double kdr = (deaths == 0) ? kills : ((double) kills / deaths);
                callback.onQueryDone(kdr);
            }
        });
    }

    public void getKills(UUID uuid, PlayerValueReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
                int kills = parseIntSafe(document.get("kills"));
                callback.onQueryDone(kills);
            }
        });
    }

    public void getDeaths(UUID uuid, PlayerValueReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
                int deaths = parseIntSafe(document.get("deaths"));
                callback.onQueryDone(deaths);
            }
        });
    }

    public void getSpy(UUID uuid, PlayerBooleanReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
                boolean spy = document.getBoolean("spy", true);
                callback.onQueryDone(spy);
            }
        });
    }

    public void getStatusCoord(UUID uuid, PlayerBooleanReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
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
