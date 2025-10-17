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
                .build();

        try {
            client = MongoClients.create(settings);
            db = client.getDatabase(dbName);
            guildas = db.getCollection("guildas");
            MineSkyGuildas.l.info(Utils.c("| Conectado ao MongoDB!"));
        } catch (Exception e) {
            MineSkyGuildas.l.severe(Utils.c("| Falha ao conectar ao MongoDB: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getGuildas() {
        return guildas;
    }

    public void close() {
        if (client != null) {
            client.close();
            MineSkyGuildas.l.info(Utils.c("| Desconectado do MongoDB."));
        }
    }
}
