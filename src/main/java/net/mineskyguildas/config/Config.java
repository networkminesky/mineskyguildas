package net.mineskyguildas.config;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    // Configurações existentes
    public static String MongoUri;
    public static String StorageDatabase;
    public static double GuildCreatePrice;
    public static int GuildTagLimit;
    public static double GuildInvitePrice;
    public static int GuildInviteDuration;

    public static double RegionWarPrice;
    public static long RegionWarDurationMinutes;

    public static void loadConfig() {
        FileConfiguration config = MineSkyGuildas.getInstance().getConfig();

        MongoUri = getString(config, "mongodb.uri");
        StorageDatabase = getString(config, "mongodb.storage-database");
        GuildCreatePrice = config.getDouble("guilds.create.price", 0);
        GuildTagLimit = config.getInt("guilds.create.tag-limit", 4);
        GuildInvitePrice = config.getDouble("guilds.invite.price", 0);
        GuildInviteDuration = config.getInt("guilds.invite.duration", 0);

        RegionWarPrice = config.getDouble("region-war.price", 5000.0);
        RegionWarDurationMinutes = config.getLong("region-war.duration-minutes", 30L);

        config.addDefault("guilds.create.price", 0.0);
        config.addDefault("guilds.create.tag-limit", 4);
        config.addDefault("guilds.invite.price", 0.0);
        config.addDefault("guilds.invite.duration", 0);
        config.addDefault("region-war.price", 5000.0);
        config.addDefault("region-war.duration-minutes", 30L);

        config.options().copyDefaults(true);
        MineSkyGuildas.getInstance().saveConfig();
    }

    private static String getString(FileConfiguration config, String path) {
        return Utils.c(config.getString(path, "&cNão foi possivel localizar a mensagem '&e" + path + "&c' do arquivo &nconfig.yml&c."));
    }
}
