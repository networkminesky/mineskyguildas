package net.mineskyguildas.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

    public static String getRegion(Player player) {
        if (player == null || player.getWorld() == null) return Utils.c("&cFora de região");

        Location loc = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return Utils.c("&cFora de região");

        ApplicableRegionSet regionSet = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        for (ProtectedRegion region : regionSet) {
            String id = region.getId().toLowerCase();
            if (id.startsWith("r_")) {
                String cleanName = id.substring(2);
                cleanName = Character.toUpperCase(cleanName.charAt(0)) + cleanName.substring(1);
                return cleanName;
            }
        }

        return Utils.c("&cFora de região");
    }
}
