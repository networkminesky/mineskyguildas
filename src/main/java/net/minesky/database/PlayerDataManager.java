package net.minesky.database;

import net.minesky.MineSkyGuildas;
import net.minesky.api.database.PlayerDatabase;
import net.minesky.api.database.UpdatedData;
import net.minesky.api.database.ValueType;
import net.minesky.config.managers.DataManager;
import net.minesky.core.databridge.callbacks.ErrorType;
import net.minesky.core.databridge.callbacks.FindOneCallback;
import net.minesky.core.databridge.callbacks.FindValueCallback;
import net.minesky.core.databridge.callbacks.SetOneCallback;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    public interface PlayerValueReturnCallback {
        public void onQueryDone(double value);
    }

    public void addKill(UUID uuid, int kill) {
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.INTEGER, "kills", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                int currentKills = (int)o;

                PlayerDatabase.setPlayerData(uuid.toString(), new UpdatedData("kills", (currentKills + kill)), new SetOneCallback() {
                    @Override
                    public void onSetDone() {}
                    @Override
                    public void onSetError(ErrorType errorType) {}
                });
            }

            @Override
            public void onQueryError(ErrorType errorType) {
                //
            }
        });
    }

    public void addDeath(UUID uuid, int death) {
        PlayerDatabase.getPlayerSpecificDataAsync(uuid.toString(), ValueType.INTEGER, "deaths", new FindValueCallback() {
            @Override
            public void onQueryDone(Document document, Object o, boolean b) {
                int currentDeaths = (int)o;

                PlayerDatabase.setPlayerData(uuid.toString(), new UpdatedData("kills", (currentDeaths + death)), new SetOneCallback() {
                    @Override
                    public void onSetDone() {}
                    @Override
                    public void onSetError(ErrorType errorType) {}
                });
            }

            @Override
            public void onQueryError(ErrorType errorType) {
                //
            }
        });
    }

    public void getKDR(UUID uuid, PlayerValueReturnCallback playerValueReturnCallback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {}

            @Override
            public void onQueryDone(Document document) {
                int kills = document.getInteger("kills");
                int deaths = document.getInteger("deaths");

                playerValueReturnCallback.onQueryDone((kills / deaths));
            }
        });
    }

    public void getKills(UUID uuid, PlayerValueReturnCallback playerValueReturnCallback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {

            }

            @Override
            public void onQueryDone(Document document) {
                int kills = document.getInteger("kills");

                playerValueReturnCallback.onQueryDone(kills);
            }
        });
    }

    public void getDeaths(UUID uuid, PlayerValueReturnCallback playerValueReturnCallback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {

            }

            @Override
            public void onQueryDone(Document document) {
                int deaths = document.getInteger("deaths");

                playerValueReturnCallback.onQueryDone(deaths);
            }
        });
    }
}
