package net.minesky;

import com.mongodb.client.MongoCollection;
import net.minesky.api.MineSkyGuildasAPI;
import net.minesky.commands.*;
import net.minesky.commands.tabcompleter.GuildTabCompleter;
import net.minesky.config.Config;
import net.minesky.config.managers.ConfigManager;
import net.minesky.config.managers.DataManager;
import net.minesky.database.MongoDBManager;
import net.minesky.database.PlayerDataManager;
import net.minesky.enums.GuildChatType;
import net.minesky.gui.GuildCreateMenu;
import net.minesky.handlers.GuildHandler;
import net.minesky.handlers.InviteHandler;
import net.minesky.handlers.RegionHandler;
import net.minesky.handlers.requests.GuildRequestManager;
import net.minesky.hooks.GuildasPlaceholder;
import net.minesky.hooks.Vault;
import net.minesky.listeners.PlayerInfo;
import net.minesky.data.Guilds;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MineSkyGuildas extends JavaPlugin {

    public static Logger l;
    public MongoDBManager mm;
    public MongoCollection<Document> coll;

    private GuildHandler handler;
    private InviteHandler inviteHandler;
    private GuildRequestManager requestManager;
    public GuildasPlaceholder guildasPlaceholder;
    private PlayerDataManager playerData;

    @Override
    public void onEnable() {
        l = this.getLogger();
        l.info("    __  ___    _                  _____    __              ______            _     __       __                ");
        l.info("   /  |/  /   (_)   ____   ___   / ___/   / /__   __  __  / ____/  __  __   (_)   / /  ____/ /  ____ _   _____");
        l.info("  / /|_/ /   / /   / __ \\ / _ \\  \\__ \\   / //_/  / / / / / / __   / / / /  / /   / /  / __  /  / __ `/  / ___/");
        l.info(" / /  / /   / /   / / / //  __/ ___/ /  / ,<    / /_/ / / /_/ /  / /_/ /  / /   / /  / /_/ /  / /_/ /  (__  ) ");
        l.info("/_/  /_/   /_/   /_/ /_/ \\___/ /____/  /_/|_|   \\__, /  \\____/   \\__,_/  /_/   /_/   \\__,_/   \\__,_/  /____/");
        l.info("                                               /____/");
        createConfigs();
        loadConfigs();
        loadMongoDB();
        handler = new GuildHandler();
        inviteHandler = new InviteHandler(this);
        requestManager = new GuildRequestManager(this);
        playerData = new PlayerDataManager();
        new RegionHandler(this);
        registerEvents();
        registerCommands();
        registerHooks();
        this.getCommand("test").setExecutor(new Test());
        initializeTicks();
        initializeAPI();
    }

    @Override
    public void onDisable() {
        l.info("GoodBye! Shutting down");
        handler.close();
    }

    private void loadMongoDB() {
        l.info("Connecting MongoDB...");

        mm = new MongoDBManager();
        mm.connect(Config.MongoUri, Config.StorageDatabase);
        coll = mm.getGuildas();
    }

    private void createConfigs() {
        l.info("Initializing plugin configurations...");
        ConfigManager.createConfig("config.yml");
    }

    public void loadConfigs() {
        Config.loadConfig();
    }

    private void registerEvents() {
        l.info("Registering event listeners...");
        getServer().getPluginManager().registerEvents(new GuildCreateMenu(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInfo(this), this);
    }

    private void registerCommands() {
        l.info("Registering plugin commands...");
        this.getCommand("guildas").setExecutor(new GuildCommand(this));
        this.getCommand(".").setExecutor(new GuildChatCommand(this, GuildChatType.GUILD));
        this.getCommand("ally").setExecutor(new GuildChatCommand(this, GuildChatType.ALLY));
        this.getCommand("líderes").setExecutor(new GuildChatCommand(this, GuildChatType.LEADER));
        this.getCommand("guildas").setTabCompleter(new GuildTabCompleter());
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            l.info("Registering setupEconomy (Vault)...");
            Vault.setupEconomy();
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            l.info("Registering PlaceholderAPI...");
            guildasPlaceholder = new GuildasPlaceholder(this);
            guildasPlaceholder.register();
        }
    }

    private void initializeTicks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Guilds guild : GuildHandler.getGuilds().values()) {
                guild.removeOldNotices();
            }
            GuildHandler.saveGuildas();
        }, 0L, 20L * 60 * 60 * 24);
    }

    private void initializeAPI() {
        l.info("Initializing plugin API...");
        MineSkyGuildasAPI.setInstance(new MineSkyGuildasAPI());
        MineSkyGuildasAPI.setPlugin(this);
    }

    public GuildHandler getGuildHandler() {
        return handler;
    }

    /*public RegionHandler getRegionHandler() {
        return new RegionHandler(this);
    }*/

    public InviteHandler getInviteHandler() {
        return inviteHandler;
    }

    public GuildRequestManager getRequestManager() {
        return requestManager;
    }

    public PlayerDataManager getPlayerData() {
        return playerData;
    }

    public static MineSkyGuildas getInstance() {
        return MineSkyGuildas.getPlugin(MineSkyGuildas.class);
    }
}
