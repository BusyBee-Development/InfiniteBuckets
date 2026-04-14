package net.busybee.InfiniteBuckets.core;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.utils.ConfigUpdater;
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
    private boolean updateCheckerEnabled;
    private boolean updateCheckerNotifyAdmins;
    private int updateCheckerInterval;
    private boolean onlyUpdateMessages;
    private int maxDrainBlocks;
    private int globalCooldown;
    private boolean asyncProcessing;
    private boolean autoDetectHooks;
    private Map<String, Boolean> enabledHooks;
    private List<String> disabledWorlds;
    private boolean defaultNetherRestriction;
    private Map<String, ConfigurationSection> worldRules;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        try {
            ConfigUpdater.updateConfig(plugin, "config.yml");
            ConfigUpdater.updateConfig(plugin, "buckets.yml");
            ConfigUpdater.updateConfig(plugin, "messages.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not update configuration files!");
            e.printStackTrace();
        }

        plugin.reloadConfig();
        cacheConfig();
    }

    private void cacheConfig() {
        FileConfiguration config = plugin.getConfig();

        debugMode = config.getBoolean("debug-mode", false);
        updateCheckerEnabled = config.getBoolean("update-checker.enabled", true);
        updateCheckerNotifyAdmins = config.getBoolean("update-checker.notify-admins", true);
        updateCheckerInterval = config.getInt("update-checker.check-interval", 24);
        onlyUpdateMessages = config.getBoolean("messages.only-update-messages", false);
        maxDrainBlocks = config.getInt("performance.max-drain-blocks", 1000);
        globalCooldown = config.getInt("performance.global-cooldown", 5);
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
        plugin.reloadConfig();
        cacheConfig();
    }

    public boolean isDebugMode() { return debugMode; }
    public boolean isUpdateCheckerEnabled() { return updateCheckerEnabled; }
    public boolean isUpdateCheckerNotifyAdmins() { return updateCheckerNotifyAdmins; }
    public int getUpdateCheckerInterval() { return updateCheckerInterval; }
    public boolean isOnlyUpdateMessages() { return onlyUpdateMessages; }
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

    public FileConfiguration getBucketsConfig() {
        File bucketsFile = new File(plugin.getDataFolder(), "buckets.yml");
        return YamlConfiguration.loadConfiguration(bucketsFile);
    }

    public FileConfiguration getMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        return YamlConfiguration.loadConfiguration(messagesFile);
    }
}
