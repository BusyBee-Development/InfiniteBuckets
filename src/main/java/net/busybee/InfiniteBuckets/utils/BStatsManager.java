package net.busybee.InfiniteBuckets.utils;

import net.busybee.InfiniteBuckets.Main;
import org.bstats.bukkit.Metrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BStatsManager {
    public BStatsManager(Main plugin) {
        int id = loadId(plugin);
        if (id != -1) {
            new Metrics(plugin, id);
        }
    }

    private int loadId(Main plugin) {
        Properties props = new Properties();
        try (InputStream is = plugin.getResource("bstats.properties")) {
            if (is != null) {
                props.load(is);
                String idStr = props.getProperty("id");
                if (idStr != null) {
                    return Integer.parseInt(idStr.trim());
                }
            }
        } catch (IOException | NumberFormatException ignored) {}
        return -1;
    }
}
