package me.djtmk.InfiniteBuckets.item;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.utils.DebugLogger;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ItemEvents implements Listener {

    private final PlatformScheduler scheduler;
    private final BucketRegistry registry;
    private final MessageManager messages;
    private final DebugLogger debugLogger;

    public ItemEvents(@NotNull Main plugin) {
        this.scheduler = Main.scheduler();
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
        this.debugLogger = plugin.getDebugLogger();
        boolean isSuperiorSkyblockEnabled = plugin.getServer().getPluginManager().isPluginEnabled("SuperiorSkyblock2");
        this.debugLogger.debug("ItemEvents initialized. SuperiorSkyblock2 enabled: " + isSuperiorSkyblockEnabled);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        // Prevent infinite buckets from being moved to offhand so duping is improbable
        if (registry.getBucket(event.getMainHandItem()).isPresent() ||
            registry.getBucket(event.getOffHandItem()).isPresent()) {
            event.setCancelled(true);
            debugLogger.debug("Prevented " + event.getPlayer().getName() + " from swapping infinite bucket to off-hand");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Prevent dragging infinite buckets into off-hand slot
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getClickedInventory() instanceof PlayerInventory)) {
            return;
        }

        // Check if player is trying to move an infinite bucket to off-hand slot (slot 40)
        if (event.getSlot() == 40 && registry.getBucket(event.getCursor()).isPresent()) {
            event.setCancelled(true);
            debugLogger.debug("Prevented " + player.getName() + " from placing infinite bucket in off-hand slot");
            return;
        }

        // Check if shift-clicking an infinite bucket that might go to off-hand
        if (event.isShiftClick() && registry.getBucket(event.getCurrentItem()).isPresent()) {
            // Check if off-hand is empty - shift click might move it there
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                event.setCancelled(true);
                debugLogger.debug("Prevented " + player.getName() + " from shift-clicking infinite bucket to off-hand");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }

        Optional<InfiniteBucket> bucketOptional = registry.getBucket(event.getItem());
        if (bucketOptional.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        InfiniteBucket bucket = bucketOptional.get();

        debugLogger.debug("Player " + player.getName() + " attempting to use " + bucket.id() + " bucket with mode " + bucket.mode());

        if (!player.hasPermission(bucket.getUsePermission())) {
            debugLogger.debug("Player " + player.getName() + " does not have permission: " + bucket.getUsePermission());
            messages.send(player, "no-permission-use", Placeholder.component("bucket_name", bucket.displayName()));
            return;
        }

        // Handle different bucket modes
        if (bucket.mode() == InfiniteBucket.BucketMode.DRAIN_AREA) {
            handleDrainAreaBucket(player, bucket, event);
        } else if (bucket.mode() == InfiniteBucket.BucketMode.EFFECT) {
            handleEffectBucket(player, bucket);
        } else {
            handleVanillaLikeBucket(player, bucket, event);
        }
    }

    private void handleEffectBucket(@NotNull Player player, @NotNull InfiniteBucket bucket) {
        if ("CLEAR_EFFECTS".equalsIgnoreCase(bucket.action())) {
            if (player.getActivePotionEffects().isEmpty()) {
                return;
            }

            // Clear all potion effects
            scheduler.runAtEntity(player, task -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
            debugLogger.debug("Cleared potion effects for " + player.getName() + " using " + bucket.id());
        }
    }

    private void handleDrainAreaBucket(@NotNull Player player, @NotNull InfiniteBucket bucket, @NotNull PlayerInteractEvent event) {
        InfiniteBucket.DrainBehavior behavior = bucket.drainBehavior();
        if (behavior == null) {
            debugLogger.debug("Drain area bucket " + bucket.id() + " has no drain behavior configured.");
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Block targetBlock;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            if (!clickedBlock.isPassable()) {
                targetBlock = clickedBlock.getRelative(event.getBlockFace());
            } else {
                targetBlock = clickedBlock;
            }
        } else {
            targetBlock = player.getTargetBlock(null, 5);
        }

        debugLogger.debug("Draining area centered at " + targetBlock.getLocation() + " with radius " + behavior.radius());

        int blocksRemoved = 0;
        int radius = behavior.radius();
        int maxBlocks = behavior.maxBlocksPerUse();

        boolean canDrainWater = behavior.drainFluids().contains("minecraft:water");
        boolean canDrainLava = behavior.drainFluids().contains("minecraft:lava");

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (blocksRemoved >= maxBlocks) {
                        break;
                    }

                    Block currentBlock = targetBlock.getRelative(x, y, z);
                    Material blockType = currentBlock.getType();
                    BlockData blockData = currentBlock.getBlockData();

                    if (canDrainWater && behavior.waterlogged() && blockData instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
                        waterlogged.setWaterlogged(false);
                        currentBlock.setBlockData(waterlogged);
                        blocksRemoved++;
                        debugLogger.debug("Removed waterlogging from block at " + currentBlock.getLocation());
                    } else if ((canDrainWater && blockType == Material.WATER) || (canDrainLava && blockType == Material.LAVA)) {
                        currentBlock.setType(Material.AIR);
                        blocksRemoved++;
                        debugLogger.debug("Drained block at " + currentBlock.getLocation());
                    }
                }

                if (blocksRemoved >= maxBlocks) {
                    break;
                }
            }

            if (blocksRemoved >= maxBlocks) {
                break;
            }
        }

        debugLogger.debug("Drained " + blocksRemoved + " blocks with " + bucket.id() + " bucket.");
        if (blocksRemoved > 0) {
            messages.send(player, "drain-success",
                    Placeholder.component("bucket_name", bucket.displayName()),
                    Placeholder.component("blocks_drained", net.kyori.adventure.text.Component.text(blocksRemoved))
            );
        }
    }

    private void handleVanillaLikeBucket(@NotNull Player player, @NotNull InfiniteBucket bucket, @NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Material placeMaterial = (bucket.material() == Material.WATER_BUCKET) ? Material.WATER : Material.LAVA;

        if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material clickedMaterial = clickedBlock.getType();
            BlockData blockData = clickedBlock.getBlockData();

            if (placeMaterial == Material.WATER && (clickedMaterial == Material.CAULDRON || clickedMaterial == Material.WATER_CAULDRON)) {
                if (blockData instanceof Levelled levelled) { // It's a water cauldron
                    if (levelled.getLevel() < levelled.getMaximumLevel()) {
                        levelled.setLevel(levelled.getLevel() + 1);
                        clickedBlock.setBlockData(levelled);
                        debugLogger.debug("Increased water cauldron level at " + clickedBlock.getLocation());
                        return;
                    }
                } else if (clickedMaterial == Material.CAULDRON) { // It's an empty cauldron
                    clickedBlock.setType(Material.WATER_CAULDRON);
                    debugLogger.debug("Filled empty cauldron with water at " + clickedBlock.getLocation());
                    return;
                }
            }

            if (placeMaterial == Material.LAVA && clickedMaterial == Material.CAULDRON) {
                clickedBlock.setType(Material.LAVA_CAULDRON);
                debugLogger.debug("Filled empty cauldron with lava at " + clickedBlock.getLocation());
                return;
            }
        }

        debugLogger.debug("Player " + player.getName() + " using " + bucket.id() + " bucket with material " + placeMaterial);

        if (clickedBlock != null && placeMaterial == Material.WATER && clickedBlock.getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()) {
            debugLogger.debug("Setting waterlogged block at " + clickedBlock.getLocation());
            waterlogged.setWaterlogged(true);
            clickedBlock.setBlockData(waterlogged);
            return;
        }

        Block blockToPlaceIn = null;
        if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            blockToPlaceIn = clickedBlock.getRelative(event.getBlockFace());
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            blockToPlaceIn = player.getTargetBlock(null, 5);
        }

        if (blockToPlaceIn != null && (blockToPlaceIn.isPassable() || blockToPlaceIn.isLiquid())) {
            if (placeMaterial == Material.WATER && blockToPlaceIn.getBlockData() instanceof Waterlogged waterlogged) {
                debugLogger.debug("Setting waterlogged block at " + blockToPlaceIn.getLocation());
                waterlogged.setWaterlogged(true);
                blockToPlaceIn.setBlockData(waterlogged);
            } else {
                debugLogger.debug("Setting block type to " + placeMaterial + " at " + blockToPlaceIn.getLocation());
                blockToPlaceIn.setType(placeMaterial);
            }
        } else if (blockToPlaceIn != null) {
            debugLogger.debug("Cannot place " + placeMaterial + " at " + blockToPlaceIn.getLocation() + " - block is not passable or liquid");
        }
    }

    private boolean hasIslandPermission(@NotNull Player player) {
        Island island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        if (island == null) return true;
        return island.hasPermission(player, IslandPrivilege.getByName("BUILD"));
    }
}
