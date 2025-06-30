package net.minesky.config.managers;

import net.minesky.MineSkyGuildas;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DataManager {
    public static void createFolder(String folder) {
        try {
            File pasta = new File(MineSkyGuildas.getInstance().getDataFolder() + File.separator + folder);
            if (!pasta.exists()) {
                pasta.mkdirs();
                MineSkyGuildas.l.info("| Criando a pasta " + folder + "...");
            }
        } catch (Throwable e) {
            MineSkyGuildas.l.severe("Não foi possível criar a pasta " + folder + ".");
            e.printStackTrace();
        }
    }

    public static void createFile(File file) {
        try {
            MineSkyGuildas.l.info("| Criando o arquivo " + file + "...");
            file.createNewFile();
        } catch (Throwable e) {
            MineSkyGuildas.l.severe("Não foi possível criar o arquivo " + file + ".");
            e.printStackTrace();
        }
    }

    public static File getFolder(String folder) {
        return new File(MineSkyGuildas.getInstance().getDataFolder() + File.separator + folder);
    }

    public static File getFile(String file, String folder) {
        return new File(MineSkyGuildas.getInstance().getDataFolder() + File.separator + folder, file);
    }

    public static File getFile(String file) {
        return new File(MineSkyGuildas.getInstance().getDataFolder() + File.separator + file);
    }

    public static FileConfiguration getConfiguration(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void deleteFile(File file) {
        file.delete();
    }
}
