package net.mineskyguildas.config.managers;

import net.mineskyguildas.MineSkyGuildas;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConfigManager {
    public static YamlConfiguration yml;
    public static void createConfig(String file) {
        if (!new File(MineSkyGuildas.getInstance().getDataFolder(), file).exists()) {
            MineSkyGuildas.l.info("| Criando a config " + file + "...");
            MineSkyGuildas.getInstance().saveResource(file, false);
        }
    }

    public static void createConfig(String file, String folder) {
        File dataFolder = new File(MineSkyGuildas.getInstance().getDataFolder(), folder);
        if (!dataFolder.exists()) {
            MineSkyGuildas.l.info("| Crinado a pasta " + folder + "...");
            dataFolder.mkdirs();
        }
        File configFolder = new File(dataFolder, file);
        yml = YamlConfiguration.loadConfiguration(configFolder);
        if (!configFolder.exists()) {
            InputStreamReader defConfigFolder = new InputStreamReader(Objects.requireNonNull(MineSkyGuildas.getInstance().getResource(file)));
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defConfigFolder);
            yml.setDefaults(defaultConfig);
            try {
                MineSkyGuildas.l.info("| Criando a config " + file + "...");
                defaultConfig.save(configFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static FileConfiguration getConfig(String file) {
        File arquivo = new File(MineSkyGuildas.getInstance().getDataFolder() + File.separator + file);
        return YamlConfiguration.loadConfiguration(arquivo);
    }

    public static FileConfiguration getConfig(String file, String folder) {
        File dataFolder = new File(MineSkyGuildas.getInstance().getDataFolder(), folder);
        File configFolder = new File(dataFolder, file);
        return YamlConfiguration.loadConfiguration(configFolder);
    }
}
