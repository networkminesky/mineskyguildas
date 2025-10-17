package net.mineskyguildas.handlers.requests;

import net.mineskyguildas.MineSkyGuildas;
import java.util.EnumMap;
import java.util.Map;

public class GuildRequestManager {
    private final Map<GuildRequestType, GuildRequestHandler> handlers = new EnumMap<>(GuildRequestType.class);

    public GuildRequestManager(MineSkyGuildas plugin) {
        for (GuildRequestType type : GuildRequestType.values())
            handlers.put(type, new GuildRequestHandler(plugin, type));
    }

    public GuildRequestHandler getHandler(GuildRequestType type) {
        return handlers.get(type);
    }
}
