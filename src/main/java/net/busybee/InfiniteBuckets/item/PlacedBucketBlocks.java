package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Remembers which blocks are placed drain buckets (place-block), so breaking one gives the bucket back.
 * Stored in the chunk's persistent data, keyed by the block position, so it survives restarts.
 */
public final class PlacedBucketBlocks {

    private final Main plugin;
    private final BucketRegistry registry;

    public PlacedBucketBlocks(@NotNull Main plugin) {
        this.plugin = plugin;
        this.registry = plugin.getBucketRegistry();
    }

    /** A placed bucket: its template and the uses it has left (-1 for unlimited). */
    public record Placed(@NotNull String bucketId, int uses) {}

    private NamespacedKey key(Block block) {
        return new NamespacedKey(plugin, "placed_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    public void mark(@NotNull Block block, @NotNull String bucketId, int uses) {
        PersistentDataContainer chunk = block.getChunk().getPersistentDataContainer();
        PersistentDataContainer data = chunk.getAdapterContext().newPersistentDataContainer();
        data.set(BucketFactory.BUCKET_ID_KEY, PersistentDataType.STRING, bucketId);
        data.set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER, uses);
        chunk.set(key(block), PersistentDataType.TAG_CONTAINER, data);
    }

    public @Nullable Placed get(@NotNull Block block) {
        PersistentDataContainer data = block.getChunk().getPersistentDataContainer().get(key(block), PersistentDataType.TAG_CONTAINER);
        if (data == null) return null;
        String id = data.get(BucketFactory.BUCKET_ID_KEY, PersistentDataType.STRING);
        Integer uses = data.get(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER);
        return id != null ? new Placed(id, uses != null ? uses : -1) : null;
    }

    public void clear(@NotNull Block block) {
        block.getChunk().getPersistentDataContainer().remove(key(block));
    }

    /** Removes and returns the mark on {@code block}, or null if it has none. */
    public @Nullable Placed take(@NotNull Block block) {
        Placed placed = get(block);
        if (placed != null) clear(block);
        return placed;
    }

    /** The bucket item a placed bucket breaks into, or null if its bucket no longer exists. */
    public @Nullable ItemStack itemFor(@NotNull Placed placed) {
        BucketTemplate template = registry.getTemplate(placed.bucketId()).orElse(null);
        if (template == null) return null;
        ItemStack item = BucketFactory.createBucket(template);
        if (item == null) return null;
        if (template.getUsageLimit() > 0 && placed.uses() > 0) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER,
                    Math.min(placed.uses(), template.getUsageLimit()));
            item.setItemMeta(meta);
        }
        return item;
    }
}
