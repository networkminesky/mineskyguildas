package net.minesky.handlers;

import com.mongodb.client.MongoCollection;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minesky.MineSkyGuildas;
import net.minesky.config.managers.DataManager;
import net.minesky.data.RegionData;
import net.minesky.hooks.WorldGuardHook;
import net.minesky.utils.Utils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionHandler {
    private final MineSkyGuildas plugin;
    private File file;
    private FileConfiguration config;
    private Map<String, RegionData> regions;
//    private Map<String, GuildWar> activeWars;
    private GuildHandler guildHandler;

    public RegionHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
        this.file = DataManager.getFile("regions.yml");
        if (!file.exists()) {
            DataManager.createFile(file);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.regions = new HashMap<>();
        this.guildHandler = plugin.getGuildHandler();
//        this.activeWars = new HashMap<>();
        loadRegions();
        startWarTick();
    }

    private void loadRegions() {
        ConfigurationSection section = config.getConfigurationSection("regions");
        if (section == null) return;


        for (String regionName : section.getKeys(false)) {
            String path = "regions." + regionName;
            String ownerGuildId = config.getString(path + ".ownerGuildId");
            String attackerId = config.getString(path + ".currentAttackerId");
            long warStartTime = config.getLong(path + ".warStartTime");
            boolean inWar = config.getBoolean(path + ".inWar");

            if (ownerGuildId != null && guildHandler.getGuildByID(ownerGuildId) == null) {
                MineSkyGuildas.l.warning("Região '" + regionName + "' tinha dono inválido. Removendo.");
                ownerGuildId = null;
            }

            if (attackerId != null && guildHandler.getGuildByID(attackerId) == null) {
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
        MineSkyGuildas.l.info(Utils.c("| " + regions.size() + " regiões de guerra carregadas."));
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

    private void startWarTick() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkAndAddWorldGuardRegions();

           /* Iterator<Map.Entry<String, GuildWar>> iterator = activeWars.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, GuildWar> entry = iterator.next();
                String regionName = entry.getKey();
                GuildWar war = entry.getValue();

                // Verifica se as guildas envolvidas na guerra ainda existem
                Guilds attackingGuild = guildHandler.getGuildByID(war.getAttackingGuildId());
                Guilds defendingGuild = (war.getDefendingGuildId() != null) ? guildHandler.getGuildByID(war.getDefendingGuildId()) : null;

                if (attackingGuild == null) {
                    MineSkyGuildas.l.info("Guerra na região " + regionName + " cancelada: Guilda atacante (" + war.getAttackingGuildId() + ") não encontrada.");
                    resolveWar(regionName); // Força resolução para limpar o estado
                    iterator.remove();
                    continue;
                }
                // Se existe um defensor e ele não foi encontrado
                if (war.getDefendingGuildId() != null && defendingGuild == null) {
                    MineSkyGuildas.l.info("Guerra na região " + regionName + " encerrada: Guilda defensora (" + war.getDefendingGuildId() + ") não encontrada. Atacante vence por WO.");
                    // Define o atacante como vencedor e resolve a guerra
                    RegionData regionData = regions.get(regionName);
                    if (regionData != null) {
                        regionData.setOwnerGuildId(war.getAttackingGuildId()); // Atacante vence por WO
                        regionData.setInWar(false);
                        regionData.setCurrentAttackerId(null);
                        regionData.setWarStartTime(0);
                        saveRegion(regionData);
                        war.notifyWarStatus(plugin, Utils.c("&a✅ GUERRA ENCERRADA! A guilda &2" + attackingGuild.getName() + " &aconquistou a região &f" + regionName + "&a (defensora não encontrada)!"));
                        Bukkit.broadcastMessage(Utils.c("&a✅ GUERRA ENCERRADA! A guilda &2" + attackingGuild.getName() + " &aconquistou a região &f" + regionName + "&a (defensora não encontrada)!"));
                    }
                    iterator.remove();
                    continue;
                }


                if (war.isWarOver()) {
                    MineSkyGuildas.l.info("Guerra pela região " + regionName + " terminou pelo tempo. Resolvendo agora.");
                    resolveWar(regionName);
                    iterator.remove(); // Remove a guerra da lista de ativas
                } else {
                    // Opcional: Enviar atualizações de status para os jogadores na região de guerra
                    long remainingTime = (war.getStartTime() + war.getDurationMillis()) - System.currentTimeMillis();
                    // Envia mensagem a cada 5 minutos
                    if (remainingTime > 0 && (remainingTime / (60 * 1000)) % 5 == 0 && remainingTime % (60 * 1000) < 2000) {
                        int minutes = (int) (remainingTime / (60 * 1000));
                        war.notifyWarStatus(plugin, Utils.c("&e⏳ &6A guerra pela região &f" + regionName + " &6termina em &e" + minutes + " minuto(s)!"));
                    }
                }
            }*/
        }, 0L, 20L * 5);
    }

    public Map<String, RegionData> getRegions() {
        return regions;
    }

    public RegionData getRegionData(String regionName) {
        return regions.get(regionName);
    }
}
