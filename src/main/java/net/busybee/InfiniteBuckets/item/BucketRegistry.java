package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketStorage;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.busybee.InfiniteBuckets.bucket.BucketFactory.BUCKET_ID_KEY;

public final class BucketRegistry {

    private final Main plugin;
    private final BucketStorage storage;
    private final Map<String, BucketTemplate> templateMap = new ConcurrentHashMap<>();

    public BucketRegistry(@NotNull Main plugin) {
        this.plugin = plugin;
        this.storage = new BucketStorage(plugin);
        this.storage.migrate();
        this.loadBuckets();
    }

    public void reload() {
        this.templateMap.clear();
        this.loadBuckets();
    }

    private void loadBuckets() {
        List<BucketTemplate> templates = storage.loadTemplates();
        for (BucketTemplate template : templates) {
            templateMap.put(template.getId(), template);
        }
        plugin.getLogger().info("Loaded " + templateMap.size() + " bucket templates.");
    }

    public Optional<BucketTemplate> getTemplate(String id) {
        return Optional.ofNullable(templateMap.get(id));
    }

    public Optional<BucketTemplate> getTemplate(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(BUCKET_ID_KEY, PersistentDataType.STRING)) {
            String id = container.get(BUCKET_ID_KEY, PersistentDataType.STRING);
            return getTemplate(id);
        }

        return Optional.empty();
    }

    public Collection<BucketTemplate> getRegisteredTemplates() {
        return Collections.unmodifiableCollection(templateMap.values());
    }

    public void registerTemplate(BucketTemplate template) {
        templateMap.put(template.getId(), template);
        storage.saveTemplate(template);
    }

    public void unregisterTemplate(String id) {
        templateMap.remove(id);
        storage.deleteTemplate(id);
    }
}
