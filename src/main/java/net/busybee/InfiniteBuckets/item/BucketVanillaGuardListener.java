package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.scheduling.BucketScheduler;
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

import java.util.Optional;

public final class BucketVanillaGuardListener implements Listener {

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

        ItemStack restore = original.clone();
        scheduler.platform().runAtEntity(player, task -> inventory.setItem(hand, restore));
    }
}
