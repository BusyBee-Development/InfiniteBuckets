package net.busybee.InfiniteBuckets.core;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.utils.ConfigMigrator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final Main plugin;
    private boolean debugMode;
    private int maxDrainBlocks;
    private int globalCooldown;
    private boolean asyncProcessing;
    private boolean autoDetectHooks;
    private Map<String, Boolean> enabledHooks;
    private List<String> disabledWorlds;
    private boolean defaultNetherRestriction;
    private Map<String, ConfigurationSection> worldRules;
    private FileConfiguration bucketsConfig;
    private File bucketsFile;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration guisConfig;
    private File guisFile;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        
        migrator.migrate("config.yml");
        plugin.reloadConfig();

        this.bucketsFile = new File(plugin.getDataFolder(), "buckets.yml");
        this.bucketsConfig = migrator.migrate("buckets.yml", bucketsFile);
        if (this.bucketsConfig == null) this.bucketsConfig = YamlConfiguration.loadConfiguration(bucketsFile);

        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messagesConfig = migrator.migrate("messages.yml", messagesFile);
        if (this.messagesConfig == null) this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        this.guisFile = new File(plugin.getDataFolder(), "guis.yml");
        this.guisConfig = migrator.migrate("guis.yml", guisFile);
        if (this.guisConfig == null) this.guisConfig = YamlConfiguration.loadConfiguration(guisFile);

        cacheConfig();
    }

    private void cacheConfig() {
        FileConfiguration config = plugin.getConfig();

        debugMode = config.getBoolean("debug-mode", false);
        maxDrainBlocks = config.getInt("performance.max-drain-blocks", 100);
        globalCooldown = config.getInt("global-cooldown", 0);
        asyncProcessing = config.getBoolean("performance.async-processing", true);
        autoDetectHooks = config.getBoolean("integrations.auto-detect-hooks", true);

        enabledHooks = new HashMap<>();
        ConfigurationSection hooksSection = config.getConfigurationSection("integrations.hooks");
        if (hooksSection != null) {
            for (String key : hooksSection.getKeys(false)) {
                enabledHooks.put(key.toLowerCase(), hooksSection.getBoolean(key));
            }
        }

        disabledWorlds = config.getStringList("world-settings.disabled-worlds");
        defaultNetherRestriction = config.getBoolean("world-settings.default-nether-restriction", true);

        worldRules = new HashMap<>();
        ConfigurationSection worldRulesSection = config.getConfigurationSection("world-settings.world-rules");
        if (worldRulesSection != null) {
            for (String key : worldRulesSection.getKeys(false)) {
                worldRules.put(key, worldRulesSection.getConfigurationSection(key));
            }
        }
    }

    public void reload() {
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        
        migrator.migrate("config.yml");
        plugin.reloadConfig();

        this.bucketsConfig = migrator.migrate("buckets.yml", bucketsFile);
        if (this.bucketsConfig == null) this.bucketsConfig = YamlConfiguration.loadConfiguration(bucketsFile);

        this.messagesConfig = migrator.migrate("messages.yml", messagesFile);
        if (this.messagesConfig == null) this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        this.guisConfig = migrator.migrate("guis.yml", guisFile);
        if (this.guisConfig == null) this.guisConfig = YamlConfiguration.loadConfiguration(guisFile);

        cacheConfig();
    }

    public boolean isDebugMode() { return debugMode; }
    public int getMaxDrainBlocks() { return maxDrainBlocks; }
    public int getGlobalCooldown() { return globalCooldown; }
    public boolean isAsyncProcessing() { return asyncProcessing; }
    public boolean isAutoDetectHooks() { return autoDetectHooks; }
    public boolean isHookEnabled(String hookName) {
        return enabledHooks.getOrDefault(hookName.toLowerCase(), true);
    }
    public List<String> getDisabledWorlds() { return disabledWorlds; }
    public boolean isDefaultNetherRestriction() { return defaultNetherRestriction; }
    public ConfigurationSection getWorldRule(String worldName) {
        return worldRules.get(worldName);
    }

    /**
     * Checks world-settings.* against a placement of the given liquid type in
     * the given world. Returns a messages.yml key to deny with, or null if
     * placement is allowed.
     */
    public String checkWorldRestriction(World world, Material liquidType) {
        String worldName = world.getName();
        if (disabledWorlds.contains(worldName)) {
            return "world-disabled";
        }

        ConfigurationSection rule = worldRules.get(worldName);
        if (rule != null) {
            String key = liquidType == Material.LAVA ? "allow-lava" : "allow-water";
            if (!rule.getBoolean(key, true)) {
                return "bucket-disabled-world";
            }
            return null;
        }

        if (defaultNetherRestriction && world.getEnvironment() == World.Environment.NETHER && liquidType == Material.WATER) {
            return "nether-disabled";
        }

        return null;
    }

    public FileConfiguration getBucketsConfig() {
        return bucketsConfig;
    }

    public void saveBucketsConfig() {
        try {
            bucketsConfig.save(bucketsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save buckets.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getGuisConfig() {
        return guisConfig;
    }
}
