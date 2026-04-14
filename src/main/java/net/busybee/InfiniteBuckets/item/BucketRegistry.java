package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BucketRegistry {

    private final Main plugin;
    private final Map<String, InfiniteBucket> bucketMap = new ConcurrentHashMap<>();
    private final Set<Material> bucketMaterials = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public BucketRegistry(@NotNull Main plugin) {
        this.plugin = plugin;
        this.loadBuckets();
    }

    public void reload() {
        this.bucketMap.clear();
        this.bucketMaterials.clear();
        this.loadBuckets();
    }

    private void loadBuckets() {
        FileConfiguration bucketsConfig = plugin.getConfigManager().getBucketsConfig();

        ConfigurationSection presetsSection = bucketsConfig.getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String key : presetsSection.getKeys(false)) {
                ConfigurationSection presetSection = presetsSection.getConfigurationSection(key);
                if (presetSection != null && presetSection.getBoolean("enabled", true)) {
                    InfiniteBucket.fromBucketsConfig(plugin, presetSection)
                            .ifPresent(this::registerBucket);
                }
            }
        }

        List<?> customBucketsList = bucketsConfig.getList("customBuckets");
        if (customBucketsList != null) {
            for (Object bucketObj : customBucketsList) {
                if (bucketObj instanceof Map<?, ?> bucketMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bucketData = (Map<String, Object>) bucketMap;

                    MemoryConfiguration memConfig = new MemoryConfiguration();
                    for (Map.Entry<String, Object> entry : bucketData.entrySet()) {
                        memConfig.set(entry.getKey(), entry.getValue());
                    }
                    
                    if (memConfig.getBoolean("enabled", true)) {
                        InfiniteBucket.fromBucketsConfig(plugin, memConfig)
                                .ifPresent(bucket -> {
                                    if (this.bucketMap.containsKey(bucket.id())) {
                                        plugin.getLogger().warning("Custom bucket overrides preset: " + bucket.id());
                                    }
                                    this.registerBucket(bucket);
                                });
                    }
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + bucketMap.size() + " valid infinite buckets from buckets.yml");
    }

    private void registerBucket(InfiniteBucket bucket) {
        bucketMap.put(bucket.id(), bucket);
        bucketMaterials.add(bucket.material());
        plugin.getDebugLogger().debug("Registered bucket: " + bucket.id() + " (Material: " + bucket.material() + ")");
    }

    public Optional<InfiniteBucket> getBucket(String id) {
        return Optional.ofNullable(bucketMap.get(id));
    }

    public Optional<InfiniteBucket> getBucket(@Nullable ItemStack item) {
        if (item == null || !bucketMaterials.contains(item.getType())) {
            return Optional.empty();
        }

        if (!item.hasItemMeta()) {
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

    public Set<Material> getBucketMaterials() {
        return Collections.unmodifiableSet(bucketMaterials);
    }

    public Collection<InfiniteBucket> getRegisteredBuckets() {
        return bucketMap.values();
    }
}
