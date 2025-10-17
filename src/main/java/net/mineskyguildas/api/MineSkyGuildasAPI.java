package net.mineskyguildas.api;

import net.mineskyguildas.MineSkyGuildas;

public class MineSkyGuildasAPI {
    private static MineSkyGuildas plugin;

    public static MineSkyGuildas getInstance() {
        return plugin;
    }

    public static void setPlugin(MineSkyGuildas plugin) {
        MineSkyGuildasAPI.plugin = plugin;
    }
}
