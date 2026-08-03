package net.mineskyguildas.hooks;

import net.mineskyguildas.data.Guilds;
import org.bukkit.Location;

public interface ClaimHook {
    Guilds getClaimOwnerGuildAt(Location loc);
}