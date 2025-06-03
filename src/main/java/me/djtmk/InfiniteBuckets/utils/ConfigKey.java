package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import java.util.List;

/**
 * Enum representing configuration keys for the InfiniteBuckets plugin.
 * Provides type-safe access to configuration values.
 */
public enum ConfigKey {
    // General settings
    DEBUG("debug"),
    LAST_NOTIFIED_VERSION("lastNotifiedVersion"),
    
    // Water bucket settings
    WATER_DISPLAY("water.display"),
    WATER_LORE("water.lore"),
    WATER_WORK_IN_NETHER("water.work_in_nether"),
    
    // Lava bucket settings
    LAVA_DISPLAY("lava.display"),
    LAVA_LORE("lava.lore");
    
    private final String path;
    
    ConfigKey(String path) {
        this.path = path;
    }
    
    /**
     * Gets the configuration path for this key.
     *
     * @return The configuration path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Gets a string value from the configuration.
     *
     * @param plugin The plugin instance
     * @param defaultValue The default value to return if the path doesn't exist
     * @return The string value from the configuration, or the default value if not found
     */
    public String getString(Main plugin, String defaultValue) {
        return plugin.getConfig().getString(path, defaultValue);
    }
    
    /**
     * Gets a boolean value from the configuration.
     *
     * @param plugin The plugin instance
     * @param defaultValue The default value to return if the path doesn't exist
     * @return The boolean value from the configuration, or the default value if not found
     */
    public boolean getBoolean(Main plugin, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }
    
    /**
     * Gets a list of strings from the configuration.
     *
     * @param plugin The plugin instance
     * @return The list of strings from the configuration, or an empty list if not found
     */
    public List<String> getStringList(Main plugin) {
        return plugin.getConfig().getStringList(path);
    }
    
    /**
     * Sets a value in the configuration.
     *
     * @param plugin The plugin instance
     * @param value The value to set
     */
    public void set(Main plugin, Object value) {
        plugin.getConfig().set(path, value);
    }
}