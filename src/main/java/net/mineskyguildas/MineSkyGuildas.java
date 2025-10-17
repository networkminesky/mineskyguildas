package net.mineskyguildas;

import com.mongodb.client.MongoCollection;
import net.mineskyguildas.api.MineSkyGuildasAPI;
import net.mineskyguildas.commands.GuildChatCommand;
import net.mineskyguildas.commands.GuildCommand;
import net.mineskyguildas.commands.tabcompleter.GuildTabCompleter;
import net.mineskyguildas.config.Config;
import net.mineskyguildas.config.managers.ConfigManager;
import net.mineskyguildas.database.MongoDBManager;
import net.mineskyguildas.database.PlayerDataManager;
import net.mineskyguildas.enums.GuildChatType;
import net.mineskyguildas.gui.GuildCreateMenu;
import net.mineskyguildas.gui.GuildEditMenu;
import net.mineskyguildas.gui.GuildMenu;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.InviteHandler;
import net.mineskyguildas.handlers.RegionHandler;
import net.mineskyguildas.handlers.requests.GuildRequestManager;
import net.mineskyguildas.hooks.GuildasPlaceholder;
import net.mineskyguildas.hooks.Vault;
import net.mineskyguildas.listeners.PlayerEvents;
import net.mineskyguildas.data.Guilds;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MineSkyGuildas extends JavaPlugin {

    public static Logger l;
    public MongoDBManager mm;
    public MongoCollection<Document> coll;

    public GuildHandler handler;
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
        initializeTicks();
        initializeAPI();
    }

    @Override
    public void onDisable() {
        l.info("GoodBye! Shutting down");
        handler.close();
    }

    public void loadMongoDB() {
        if (mm != null) {
            handler.close();
        }
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
        getServer().getPluginManager().registerEvents(new GuildMenu(this), this);
        getServer().getPluginManager().registerEvents(new GuildEditMenu(this), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
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
