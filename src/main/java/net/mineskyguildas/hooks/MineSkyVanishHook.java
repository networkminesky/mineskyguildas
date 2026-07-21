package net.mineskyguildas.hooks;

import net.mineskyvanish.api.vanish.VanishAPI;
import org.bukkit.entity.Player;

public class MineSkyVanishHook {
    public static boolean isPlayerVanished(Player player) {
        return VanishAPI.isInvisible(player);
    }
}
