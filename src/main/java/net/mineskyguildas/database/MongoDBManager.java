package net.mineskyguildas.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.utils.Utils;
import org.bson.Document;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MongoDBManager {
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> guildas;

    public void connect(String uri, String dbName) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                )

                .applyToClusterSettings(builder -> builder
                        .serverSelectionTimeout(5, TimeUnit.SECONDS)
                )

                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(30)
                        .minSize(2)
                        .maxWaitTime(3, TimeUnit.SECONDS)
                        .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                        .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                )
                .build();

        try {
            client = MongoClients.create(settings);
            db = client.getDatabase(dbName);
            guildas = db.getCollection("guildas");
            MineSkyGuildas.l.info(Utils.c("| Conectado ao MongoDB com sucesso!"));
        } catch (Exception e) {
            MineSkyGuildas.l.severe(Utils.c("| Falha ao conectar ao MongoDB: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getGuildas() {
        return guildas;
    }

    public MongoCollection<Document> getGuildWar() {
        return db != null ? db.getCollection("guild_wars") : null;
    }

    public MongoCollection<Document> getRivalryTimestamps() {
        return db != null ? db.getCollection("rivalry_timestamps") : null;
    }

    public MongoCollection<Document> getGuildTraitors() {
        return db != null ? db.getCollection("guild_traitors") : null;
    }

    public MongoCollection<Document> getGuildTraitorCooldowns() {
        return db != null ? db.getCollection("guild_traitor_cooldowns") : null;
    }

    public void close() {
        if (client != null) {
            MongoClient clientToClose = client;
            client = null;
            db = null;
            guildas = null;

            CompletableFuture.runAsync(() -> {
                try {
                    clientToClose.close();
                    MineSkyGuildas.l.info(Utils.c("| Desconectado do MongoDB com sucesso."));
                } catch (Exception e) {
                    MineSkyGuildas.l.warning(Utils.c("| Erro ao fechar conexão com MongoDB: " + e.getMessage()));
                }
            });
        }
    }

    public boolean isConnected() {
        if (client == null || db == null) {
            return false;
        }

        try {
            db.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}