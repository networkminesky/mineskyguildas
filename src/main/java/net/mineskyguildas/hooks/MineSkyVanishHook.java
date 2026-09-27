package net.mineskyguildas.hooks;

import net.mineskyvanish.api.vanish.VanishAPI;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MineSkyVanishHook {
    public static boolean isPlayerVanished(Player player) {
        return VanishAPI.isInvisible(player);
    }

    public static boolean isPlayerVanished(UUID uuid) {
        return VanishAPI.getAllInvisiblePlayers().contains(uuid);
    }
}
