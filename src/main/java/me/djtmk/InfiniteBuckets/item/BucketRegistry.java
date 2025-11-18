package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BucketRegistry {

    private final Main plugin;
    private final Map<String, InfiniteBucket> bucketMap = new ConcurrentHashMap<>();

    public BucketRegistry(@NotNull Main plugin) {
        this.plugin = plugin;
        this.loadBuckets();
    }

    public void reload() {
        this.bucketMap.clear();
        this.loadBuckets();
    }

    private void loadBuckets() {
        // Load buckets.yml configuration file
        File bucketsFile = new File(plugin.getDataFolder(), "buckets.yml");
        if (!bucketsFile.exists()) {
            plugin.saveResource("buckets.yml", false);
        }
        
        YamlConfiguration bucketsConfig = YamlConfiguration.loadConfiguration(bucketsFile);
        
        // Load preset buckets
        ConfigurationSection presetsSection = bucketsConfig.getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String key : presetsSection.getKeys(false)) {
                ConfigurationSection presetSection = presetsSection.getConfigurationSection(key);
                if (presetSection != null && presetSection.getBoolean("enabled", true)) {
                    InfiniteBucket.fromBucketsConfig(plugin, presetSection)
                            .ifPresent(bucket -> {
                                bucketMap.put(bucket.id(), bucket);
                                plugin.getDebugLogger().debug("Loaded preset bucket: " + bucket.id());
                            });
                }
            }
        }
        
        // Load custom buckets
        List<?> customBucketsList = bucketsConfig.getList("customBuckets");
        if (customBucketsList != null) {
            for (Object bucketObj : customBucketsList) {
                if (bucketObj instanceof Map<?, ?> bucketMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bucketData = (Map<String, Object>) bucketMap;
                    
                    // Convert map to ConfigurationSection for easier handling
                    MemoryConfiguration memConfig = new MemoryConfiguration();
                    for (Map.Entry<String, Object> entry : bucketData.entrySet()) {
                        memConfig.set(entry.getKey(), entry.getValue());
                    }
                    
                    if (memConfig.getBoolean("enabled", true)) {
                        InfiniteBucket.fromBucketsConfig(plugin, memConfig)
                                .ifPresent(bucket -> {
                                    // Check for ID conflicts with presets
                                    if (this.bucketMap.containsKey(bucket.id())) {
                                        plugin.getLogger().warning("Custom bucket overrides preset: " + bucket.id());
                                    }
                                    this.bucketMap.put(bucket.id(), bucket);
                                    plugin.getDebugLogger().debug("Loaded custom bucket: " + bucket.id());
                                });
                    }
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + bucketMap.size() + " valid infinite buckets from buckets.yml");
    }

    public Optional<InfiniteBucket> getBucket(String id) {
        return Optional.ofNullable(bucketMap.get(id));
    }

    public Optional<InfiniteBucket> getBucket(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(InfiniteBucket.BUCKET_ID_KEY, PersistentDataType.STRING)) {
            String id = container.get(InfiniteBucket.BUCKET_ID_KEY, PersistentDataType.STRING);
            return getBucket(id);
        }

        return Optional.empty();
    }

    public Collection<InfiniteBucket> getRegisteredBuckets() {
        return bucketMap.values();
    }
}
