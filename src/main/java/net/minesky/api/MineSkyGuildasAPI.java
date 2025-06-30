package net.minesky.api;

import net.minesky.MineSkyGuildas;
import net.minesky.handlers.GuildHandler;

public class MineSkyGuildasAPI {
    private static MineSkyGuildasAPI instance;
    private static MineSkyGuildas plugin;

    public GuildHandler getGuildHandler() {
        return plugin.getGuildHandler();
    }

    public static MineSkyGuildasAPI getInstance() {
        return instance;
    }

    public static void setInstance(MineSkyGuildasAPI api) {
        instance = api;
    }

    public static MineSkyGuildas getPlugin() {
        return plugin;
    }

    public static void setPlugin(MineSkyGuildas plugin) {
        MineSkyGuildasAPI.plugin = plugin;
    }
}
