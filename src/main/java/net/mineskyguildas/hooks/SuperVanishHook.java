package net.mineskyguildas.hooks;

import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;

public class SuperVanishHook {
    public static boolean isPlayerVanished(Player player) {
        return VanishAPI.getInvisiblePlayers().contains(player.getUniqueId());
    }
}
