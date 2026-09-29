package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.scheduling.BucketScheduler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public final class BucketVanillaGuardListener implements Listener {

    private static final Set<Material> VANILLA_BUCKETS = EnumSet.of(
            Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.POWDER_SNOW_BUCKET);

    private final BucketRegistry registry;
    private final BucketScheduler scheduler;

    public BucketVanillaGuardListener(@NotNull Main plugin) {
        this.registry = plugin.getBucketRegistry();
        this.scheduler = plugin.getBucketScheduler();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFill(@NotNull PlayerBucketFillEvent event) {
        guard(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEmpty(@NotNull PlayerBucketEmptyEvent event) {
        guard(event);
    }

    private void guard(PlayerBucketEvent event) {
        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack original = inventory.getItem(hand);

        Optional<BucketTemplate> templateOpt = registry.getTemplate(original);
        if (templateOpt.isEmpty()) return;

        event.setCancelled(true);

        // Only undo an actual vanilla swap. Restoring unconditionally would dupe the bucket if the
        // player dropped or moved it before this runs.
        ItemStack restore = original.clone();
        scheduler.platform().runAtEntity(player, task -> {
            ItemStack current = inventory.getItem(hand);
            if (isVanillaBucket(current) && registry.getTemplate(current).isEmpty()) {
                inventory.setItem(hand, restore);
            }
        });
    }

    private static boolean isVanillaBucket(ItemStack item) {
        return item != null && VANILLA_BUCKETS.contains(item.getType());
    }
}
