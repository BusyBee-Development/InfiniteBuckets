package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class ConfigUpdater {

    public static void updateConfig(Main plugin, String fileName) throws IOException {
        File configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }

        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultConfigStream = plugin.getResource(fileName);
        if (defaultConfigStream == null) {
            return;
        }
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

        boolean updated = merge(defaultConfig, userConfig);

        if (updated) {
            File backupFile = new File(plugin.getDataFolder(), fileName + ".old");
            configFile.renameTo(backupFile);
            plugin.getLogger().info("'" + fileName + "' has been updated. Your old config was saved to '" + fileName + ".old'");
            plugin.saveResource(fileName, true);
            
            FileConfiguration newSavedConfig = YamlConfiguration.loadConfiguration(configFile);
            merge(userConfig, newSavedConfig);
            newSavedConfig.save(configFile);
        }
    }

    private static boolean merge(ConfigurationSection from, ConfigurationSection to) {
        boolean updated = false;
        Set<String> fromKeys = from.getKeys(true);

        for (String key : fromKeys) {
            if (!to.isSet(key)) {
                to.set(key, from.get(key));
                updated = true;
            }
        }
        return updated;
    }
}
