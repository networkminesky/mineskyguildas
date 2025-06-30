package net.minesky.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldGuardHook {
    public static List<ProtectedRegion> getRegions(World bukkitWorld) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(bukkitWorld));

        List<ProtectedRegion> regions = new ArrayList<>();
        if (manager != null) {
            for (Map.Entry<String, ProtectedRegion> entry : manager.getRegions().entrySet()) {
                if (entry.getKey().startsWith("r_")) {
                    regions.add(entry.getValue());
                }
            }
        }

        return regions;
    }
}
