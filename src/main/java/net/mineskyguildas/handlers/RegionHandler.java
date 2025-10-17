package net.mineskyguildas.handlers;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.config.managers.DataManager;
import net.mineskyguildas.data.RegionData;
import net.mineskyguildas.hooks.WorldGuardHook;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegionHandler {
    private final MineSkyGuildas plugin;

    private final File file;
    private final FileConfiguration config;
    private final Map<String, RegionData> regions = new HashMap<>();

    public RegionHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
        this.file = DataManager.getFile("regions.yml");
        if (!file.exists()) {
            DataManager.createFile(file);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadRegions();
        tick();
    }

    private void loadRegions() {
        regions.clear();

        ConfigurationSection section = config.getConfigurationSection("regions");
        if (section == null) return;

        for (String regionName : section.getKeys(false)) {
            String path = "regions." + regionName;
            String ownerGuildId = config.getString(path + ".ownerGuildId");
            String attackerId = config.getString(path + ".currentAttackerId");
            long warStartTime = config.getLong(path + ".warStartTime");
            boolean inWar = config.getBoolean(path + ".inWar");

            if (ownerGuildId != null && GuildHandler.getGuildByID(ownerGuildId) == null) {
                MineSkyGuildas.l.warning("Região '" + regionName + "' tinha dono inválido. Removendo.");
                ownerGuildId = null;
            }

            if (attackerId != null && GuildHandler.getGuildByID(attackerId) == null) {
                MineSkyGuildas.l.warning("Região '" + regionName + "' tinha atacante inválido. Reiniciando guerra.");
                attackerId = null;
                inWar = false;
                warStartTime = 0;
            }

            RegionData data = new RegionData(regionName,
                    ownerGuildId,
                    attackerId,
                    warStartTime,
                    inWar
            );

            regions.put(regionName, data);
        }
    }

    public void dataToYml(RegionData data) {
        String path = "regions." + data.getRegionName();
        config.set(path + ".ownerGuildId", data.getOwnerGuildId());
        config.set(path + ".currentAttackerId", data.getCurrentAttackerId() != null ? data.getCurrentAttackerId().toString() : null);
        config.set(path + ".warStartTime", data.getWarStartTime());
        config.set(path + ".inWar", data.isInWar());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRegions() {
        for (RegionData data : regions.values()) {
            dataToYml(data);
        }
    }

    public void checkAndAddWorldGuardRegions() {
        Bukkit.getWorlds().forEach(world -> {
            WorldGuardHook.getRegions(world).stream()
                    .map(ProtectedRegion::getId)
                    .filter(name -> name.startsWith("r_") && !regions.containsKey(name))
                    .forEach(name -> {
                        RegionData data = new RegionData(name);
                        regions.put(name, data);
                        dataToYml(data);
                        MineSkyGuildas.l.info(Utils.c("| Nova região de guerra adicionada: " + name));
                    });
        });
    }

    private void tick() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkAndAddWorldGuardRegions, 0L, 20L * 5);
    }

    public Map<String, RegionData> getRegions() {
        return regions;
    }

    public RegionData getRegionData(String regionName) {
        return regions.get(regionName);
    }
}
