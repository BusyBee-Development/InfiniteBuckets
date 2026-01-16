package me.djtmk.InfiniteBuckets.item;

import com.tcoded.folialib.impl.PlatformScheduler;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.hooks.HookManager;
import me.djtmk.InfiniteBuckets.utils.DebugLogger;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class ItemEvents implements Listener {

    private final Main plugin;
    private final PlatformScheduler scheduler;
    private final BucketRegistry registry;
    private final MessageManager messages;
    private final DebugLogger debugLogger;
    private final HookManager hookManager;

    public ItemEvents(@NotNull Main plugin) {
        this.plugin = plugin;
        this.scheduler = Main.scheduler();
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
        this.debugLogger = plugin.getDebugLogger();
        this.hookManager = plugin.getHookManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        final ItemStack mainHandItem = event.getMainHandItem();
        final ItemStack offHandItem = event.getOffHandItem();

        if ((mainHandItem != null && registry.getBucket(mainHandItem).isPresent()) ||
            (offHandItem != null && registry.getBucket(offHandItem).isPresent())) {
            event.setCancelled(true);
            debugLogger.debug("Prevented " + event.getPlayer().getName() + " from swapping infinite bucket to off-hand");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getClickedInventory() instanceof PlayerInventory)) {
            return;
        }

        final ItemStack cursor = event.getCursor();
        if (event.getSlot() == 40 && cursor != null && registry.getBucket(cursor).isPresent()) {
            event.setCancelled(true);
            debugLogger.debug("Prevented " + player.getName() + " from placing infinite bucket in off-hand slot");
            return;
        }

        final ItemStack currentItem = event.getCurrentItem();
        if (event.isShiftClick() && currentItem != null && registry.getBucket(currentItem).isPresent()) {
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

        ItemStack itemInHand = event.getItem();
        if (itemInHand == null) {
            return;
        }

        Optional<InfiniteBucket> bucketOptional = registry.getBucket(itemInHand);
        if (bucketOptional.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        InfiniteBucket bucket = bucketOptional.get();

        debugLogger.debug("Player " + player.getName() + " attempting to use " + bucket.id() + " bucket with mode " + bucket.mode());

        FileConfiguration config = plugin.getConfig();
        List<String> disabledWorlds = config.getStringList("world-settings.disabled-worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            debugLogger.debug("Player " + player.getName() + " tried to use bucket in disabled world: " + player.getWorld().getName());
            messages.send(player, "bucket-disabled-world");
            return;
        }

        String worldName = player.getWorld().getName();
        ConfigurationSection worldRules = config.getConfigurationSection("world-settings.world-rules." + worldName);
        if (worldRules != null) {
            String ruleKey = "allow-" + bucket.id() + "-buckets";
            if (worldRules.contains(ruleKey) && !worldRules.getBoolean(ruleKey)) {
                debugLogger.debug("Bucket " + bucket.id() + " is disabled in world " + worldName + " by world-rules.");
                messages.send(player, "bucket-disabled-world");
                return;
            }
        }

        if (player.getWorld().getEnvironment() == World.Environment.NETHER && !bucket.worksInNether()) {
            debugLogger.debug("Player " + player.getName() + " tried to use bucket in Nether, but it is disabled.");
            messages.send(player, "nether-disabled");
            return;
        }

        if (!player.hasPermission(bucket.getUsePermission())) {
            debugLogger.debug("Player " + player.getName() + " does not have permission: " + bucket.getUsePermission());
            messages.send(player, "no-permission-use", Placeholder.component("bucket_name", bucket.displayName()));
            return;
        }

        if (event.getClickedBlock() != null && !hookManager.canBuild(player, event.getClickedBlock())) {
            debugLogger.debug("Player " + player.getName() + " does not have permission to use the bucket in this region.");
            messages.send(player, "no-permission-use", Placeholder.component("bucket_name", bucket.displayName()));
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        Integer currentUses = null;
        if (bucket.uses() != -1) {
            currentUses = meta.getPersistentDataContainer().get(InfiniteBucket.BUCKET_USES_KEY, PersistentDataType.INTEGER);
            if (currentUses == null) {
                currentUses = bucket.uses();
            }

            if (currentUses <= 0) {
                messages.send(player, "bucket-no-uses", Placeholder.component("bucket_name", bucket.displayName()));
                debugLogger.debug("Player " + player.getName() + " tried to use " + bucket.id() + " with 0 uses left.");
                return;
            }
        }

        boolean usedSuccessfully = false;
        if (bucket.mode() == InfiniteBucket.BucketMode.DRAIN_AREA) {
            usedSuccessfully = handleDrainAreaBucket(player, bucket, event);
        } else if (bucket.mode() == InfiniteBucket.BucketMode.EFFECT) {
            usedSuccessfully = handleEffectBucket(player, bucket);
        } else {
            usedSuccessfully = handleVanillaLikeBucket(player, bucket, event);
        }

        if (usedSuccessfully && bucket.uses() != -1 && currentUses != null) {
            currentUses--;

            if (currentUses <= 0) {
                messages.send(player, "bucket-depleted", Placeholder.component("bucket_name", bucket.displayName()));
                debugLogger.debug("Bucket " + bucket.id() + " depleted for " + player.getName());

                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);

                    // Since one bucket from the stack is consumed, we reset the uses for the remaining stack.
                    ItemMeta newMeta = itemInHand.getItemMeta();
                    newMeta.getPersistentDataContainer().set(InfiniteBucket.BUCKET_USES_KEY, PersistentDataType.INTEGER, bucket.uses());
                    List<net.kyori.adventure.text.Component> newLore = InfiniteBucket.updateLoreWithUses(bucket.lore(), bucket.uses(), bucket.uses());
                    newMeta.lore(newLore);
                    itemInHand.setItemMeta(newMeta);
                } else {
                    player.getInventory().setItem(event.getHand(), null);
                }
            } else {
                meta.getPersistentDataContainer().set(InfiniteBucket.BUCKET_USES_KEY, PersistentDataType.INTEGER, currentUses);
                List<net.kyori.adventure.text.Component> updatedLore = InfiniteBucket.updateLoreWithUses(bucket.lore(), currentUses, bucket.uses());
                meta.lore(updatedLore);
                itemInHand.setItemMeta(meta);
            }
        }
    }

    private boolean handleEffectBucket(@NotNull Player player, @NotNull InfiniteBucket bucket) {
        if ("CLEAR_EFFECTS".equalsIgnoreCase(bucket.action())) {
            if (player.getActivePotionEffects().isEmpty()) {
                return false;
            }

            // Clear all potion effects
            scheduler.runAtEntity(player, task -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
            debugLogger.debug("Cleared potion effects for " + player.getName() + " using " + bucket.id());
            return true;
        }
        return false;
    }

    private boolean handleDrainAreaBucket(@NotNull Player player, @NotNull InfiniteBucket bucket, @NotNull PlayerInteractEvent event) {
        InfiniteBucket.DrainBehavior behavior = bucket.drainBehavior();
        if (behavior == null) {
            debugLogger.debug("Drain area bucket " + bucket.id() + " has no drain behavior configured.");
            return false;
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

                    if (!hookManager.canBuild(player, currentBlock)) {
                        continue;
                    }

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
            return true;
        }
        return false;
    }

    private boolean handleVanillaLikeBucket(@NotNull Player player, @NotNull InfiniteBucket bucket, @NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Material placeMaterial = (bucket.material() == Material.WATER_BUCKET) ? Material.WATER : Material.LAVA;
        boolean placed = false;

        if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material clickedMaterial = clickedBlock.getType();
            BlockData blockData = clickedBlock.getBlockData();

            if (placeMaterial == Material.WATER && (clickedMaterial == Material.CAULDRON || clickedMaterial == Material.WATER_CAULDRON)) {
                if (blockData instanceof Levelled levelled) { // It's a water cauldron
                    if (levelled.getLevel() < levelled.getMaximumLevel()) {
                        levelled.setLevel(levelled.getLevel() + 1);
                        clickedBlock.setBlockData(levelled);
                        debugLogger.debug("Increased water cauldron level at " + clickedBlock.getLocation());
                        placed = true;
                    }
                } else if (clickedMaterial == Material.CAULDRON) { // It's an empty cauldron
                    clickedBlock.setType(Material.WATER_CAULDRON);
                    debugLogger.debug("Filled empty cauldron with water at " + clickedBlock.getLocation());
                    placed = true;
                }
            }

            if (placeMaterial == Material.LAVA && clickedMaterial == Material.CAULDRON) {
                clickedBlock.setType(Material.LAVA_CAULDRON);
                debugLogger.debug("Filled empty cauldron with lava at " + clickedBlock.getLocation());
                placed = true;
            }
        }

        if (!placed && clickedBlock != null && placeMaterial == Material.WATER && clickedBlock.getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()) {
            if (!hookManager.canBuild(player, clickedBlock)) {
                debugLogger.debug("Player " + player.getName() + " cannot waterlog block at " + clickedBlock.getLocation() + " due to region protection.");
                return false;
            }
            debugLogger.debug("Setting waterlogged block at " + clickedBlock.getLocation());
            waterlogged.setWaterlogged(true);
            clickedBlock.setBlockData(waterlogged);
            placed = true;
        }

        Block blockToPlaceIn = null;
        if (!placed && clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            blockToPlaceIn = clickedBlock.getRelative(event.getBlockFace());
        } else if (!placed && event.getAction() == Action.RIGHT_CLICK_AIR) {
            blockToPlaceIn = player.getTargetBlock(null, 5);
        }

        if (!placed && blockToPlaceIn != null && (blockToPlaceIn.isPassable() || blockToPlaceIn.isLiquid()) && blockToPlaceIn.getType() != Material.AIR) {
            if (!hookManager.canBuild(player, blockToPlaceIn)) {
                debugLogger.debug("Player " + player.getName() + " cannot place fluid at " + blockToPlaceIn.getLocation() + " due to region protection.");
                return false;
            }
            if (placeMaterial == Material.WATER && blockToPlaceIn.getBlockData() instanceof Waterlogged waterlogged) {
                debugLogger.debug("Setting waterlogged block at " + blockToPlaceIn.getLocation());
                waterlogged.setWaterlogged(true);
                blockToPlaceIn.setBlockData(waterlogged);
            } else {
                debugLogger.debug("Setting block type to " + placeMaterial + " at " + blockToPlaceIn.getLocation());
                blockToPlaceIn.setType(placeMaterial);
            }
            placed = true;
        } else if (!placed && blockToPlaceIn != null) {
            debugLogger.debug("Cannot place " + placeMaterial + " at " + blockToPlaceIn.getLocation() + " - block is not passable or liquid");
        }
        return placed;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(@NotNull BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        Optional<InfiniteBucket> bucketOptional = registry.getBucket(item);

        if (bucketOptional.isEmpty()) {
            return;
        }

        InfiniteBucket bucket = bucketOptional.get();
        debugLogger.debug("Dispenser attempting to dispense " + bucket.id() + " bucket");

        if (bucket.uses() != -1) {
            debugLogger.debug("Cannot dispense limited-use bucket " + bucket.id() + " from dispenser.");
            event.setCancelled(true);
            return;
        }

        if (bucket.mode() != InfiniteBucket.BucketMode.VANILLA_LIKE) {
            debugLogger.debug("Cannot dispense " + bucket.id() + " bucket - only VANILLA_LIKE mode is supported");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) event.getBlock().getState();
        org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) event.getBlock().getBlockData();
        Block targetBlock = event.getBlock().getRelative(directional.getFacing());
        Material placeMaterial = (bucket.material() == Material.WATER_BUCKET) ? Material.WATER : Material.LAVA;
        debugLogger.debug("Attempting to place " + placeMaterial + " at " + targetBlock.getLocation());

        boolean placed = false;

        if (targetBlock.getType() == Material.CAULDRON || targetBlock.getType() == Material.WATER_CAULDRON) {
            if (placeMaterial == Material.WATER) {
                BlockData blockData = targetBlock.getBlockData();
                if (blockData instanceof Levelled levelled && targetBlock.getType() == Material.WATER_CAULDRON) {
                    if (levelled.getLevel() < levelled.getMaximumLevel()) {
                        levelled.setLevel(levelled.getLevel() + 1);
                        targetBlock.setBlockData(levelled);
                        debugLogger.debug("Dispenser increased water cauldron level at " + targetBlock.getLocation());
                        placed = true;
                    }
                } else if (targetBlock.getType() == Material.CAULDRON) {
                    targetBlock.setType(Material.WATER_CAULDRON);
                    debugLogger.debug("Dispenser filled empty cauldron with water at " + targetBlock.getLocation());
                    placed = true;
                }
            }
        } else if (placeMaterial == Material.LAVA && targetBlock.getType() == Material.CAULDRON) {
            targetBlock.setType(Material.LAVA_CAULDRON);
            debugLogger.debug("Dispenser filled empty cauldron with lava at " + targetBlock.getLocation());
            placed = true;
        }

        if (!placed && placeMaterial == Material.WATER && targetBlock.getBlockData() instanceof Waterlogged waterlogged) {
            if (!waterlogged.isWaterlogged()) {
                waterlogged.setWaterlogged(true);
                targetBlock.setBlockData(waterlogged);
                debugLogger.debug("Dispenser waterlogged block at " + targetBlock.getLocation());
                placed = true;
            }
        }

        if (!placed && (targetBlock.isPassable() || targetBlock.isLiquid())) {
            targetBlock.setType(placeMaterial);
            debugLogger.debug("Dispenser placed " + placeMaterial + " at " + targetBlock.getLocation());
            placed = true;
        }

        if (!placed) {
            debugLogger.debug("Cannot place " + placeMaterial + " at " + targetBlock.getLocation() + " - block is not passable or liquid");
        }
    }
}
