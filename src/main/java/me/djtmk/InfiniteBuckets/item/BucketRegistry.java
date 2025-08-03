package me.djtmk.InfiniteBuckets.item;

import com.google.common.base.Preconditions;
import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BucketRegistry {

    private final Main plugin;
    private final NamespacedKey bucketKey;
    private final Map<String, InfiniteBucket> bucketMap = new HashMap<>();

    public BucketRegistry(Main plugin) {
        this.plugin = plugin;
        this.bucketKey = new NamespacedKey(plugin, "infinite_bucket");
        this.loadBuckets();
    }

    public void reload() {
        this.bucketMap.clear();
        this.loadBuckets();
    }

    private void loadBuckets() {
        ConfigurationSection bucketSection = plugin.getConfig().getConfigurationSection("buckets");
        Preconditions.checkNotNull(bucketSection, "Config is missing 'buckets' section.");

        for (String key : bucketSection.getKeys(false)) {
            ConfigurationSection individualBucketSection = bucketSection.getConfigurationSection(key);
            if (individualBucketSection != null) {
                InfiniteBucket.fromConfig(plugin, key, individualBucketSection)
                        .ifPresent(bucket -> bucketMap.put(key, bucket));
            }
        }
        plugin.getLogger().info("Loaded " + bucketMap.size() + " valid infinite buckets.");
    }

    public Optional<InfiniteBucket> getBucket(String id) {
        return Optional.ofNullable(bucketMap.get(id));
    }

    public Optional<InfiniteBucket> getBucket(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(bucketKey, PersistentDataType.STRING)) {
            String id = container.get(bucketKey, PersistentDataType.STRING);
            return getBucket(id);
        }
        return Optional.empty();
    }

    public Collection<InfiniteBucket> getRegisteredBuckets() {
        return bucketMap.values();
    }
}
