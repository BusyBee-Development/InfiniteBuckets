package me.djtmk.InfiniteBuckets.item;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ItemEvents implements Listener {

    private final Main plugin;
    private final NamespacedKey infiniteKey;

    public ItemEvents(Main plugin) {
        this.plugin = plugin;
        this.infiniteKey = new NamespacedKey(plugin, "infinite");
    }

    private boolean isSuperiorSkyblockInstalled() {
        return plugin.getServer().getPluginManager().getPlugin("SuperiorSkyblock2") != null;
    }

    private boolean islandCheck(final @NotNull Player player) {
        if (!isSuperiorSkyblockInstalled()) {
            return true;
        }
        Island island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player.getUniqueId());
        if (island == null) return true;
        if (island.getOwner().getUniqueId() == player.getUniqueId()) return true;

        if (superiorPlayer == null) return false;
        if (island.isMember(superiorPlayer) && island.hasPermission(superiorPlayer, IslandPrivilege.getByName("Build"))) return true;
        return superiorPlayer.hasBypassModeEnabled();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block clickedBlock = event.getClickedBlock();
        plugin.debugLog("PlayerInteractEvent for " + player.getName() + ", action: " + event.getAction() + ", block: " + (clickedBlock != null ? clickedBlock.getType() : "null"), item);

        if (item == null || (item.getType() != Material.WATER_BUCKET && item.getType() != Material.LAVA_BUCKET)) {
            plugin.debugLog("Not a bucket item");
            return;
        }

        if (!isInfinite(item)) {
            plugin.debugLog("Bucket is not infinite", item);
            return;
        }

        // Check if player has permission to use this bucket
        Integer bucketType = item.getItemMeta().getPersistentDataContainer().get(infiniteKey, PersistentDataType.INTEGER);
        if (bucketType == 0 && !player.hasPermission("infb.use.water")) {
            plugin.debugLog("Player " + player.getName() + " doesn't have permission to use water bucket");
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to use this bucket!");
            return;
        } else if (bucketType == 1 && !player.hasPermission("infb.use.lava")) {
            plugin.debugLog("Player " + player.getName() + " doesn't have permission to use lava bucket");
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to use this bucket!");
            return;
        }

        plugin.debugLog("Infinite bucket detected. Type: " + item.getType());

        if (clickedBlock != null && clickedBlock.getType().name().contains("CHEST")) {
            event.setCancelled(true);
            player.updateInventory();
            plugin.debugLog("Detected chest, cancelling the interaction.", new ItemStack(clickedBlock.getType()));
            return;
        }

        // Check island API
        if (!islandCheck(player)) {
            event.setCancelled(true);
            plugin.debugLog("Island check failed, cancelling interaction");
            return;
        }

        // If event is canceled or usage is denied, don't proceed
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) {
            plugin.debugLog("Event cancelled or usage denied");
            return;
        }

        plugin.debugLog("Bucket infinite type: " + (bucketType == 0 ? "Water" : "Lava"));

        Block targetBlock;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            BlockFace face = event.getBlockFace();
            assert clickedBlock != null;
            targetBlock = clickedBlock.getRelative(face);
            plugin.debugLog("Target block: " + targetBlock.getType());
        } else {
            // Right-click in air: use player's eye location or block in front
            targetBlock = player.getTargetBlockExact(5);
            if (targetBlock == null) {
                targetBlock = player.getEyeLocation().getBlock();
            }
            plugin.debugLog("Right-click air, target block: " + targetBlock.getType());
        }

        // Check if player is in their own element
        boolean isInOwnElement = (bucketType == 0 && player.isInWater()) || (bucketType == 1 && player.getLocation().getBlock().getType() == Material.LAVA);
        plugin.debugLog("Player in own element: " + isInOwnElement);

        // Handle waterlogging for water buckets
        if (bucketType == 0 && clickedBlock != null) {
            if (clickedBlock.getBlockData() instanceof Waterlogged waterlogged) {
                plugin.debugLog("Clicked block waterloggable: " + clickedBlock.getType() + ", Current waterlogged: " + waterlogged.isWaterlogged());
                if (!waterlogged.isWaterlogged()) {
                    waterlogged.setWaterlogged(true);
                    clickedBlock.setBlockData(waterlogged);
                    plugin.debugLog("Set clicked block to waterlogged: " + clickedBlock.getType());
                    event.setCancelled(true);
                    preserveBucket(player, item);
                    plugin.debugLog("Waterlogging complete, bucket preserved");
                    return;
                } else {
                    plugin.debugLog("Clicked block already waterlogged");
                }
            } else if (targetBlock.getBlockData() instanceof Waterlogged waterlogged) {
                plugin.debugLog("Target block waterloggable: " + targetBlock.getType() + ", Current waterlogged: " + waterlogged.isWaterlogged());
                if (!waterlogged.isWaterlogged()) {
                    waterlogged.setWaterlogged(true);
                    targetBlock.setBlockData(waterlogged);
                    plugin.debugLog("Set target block to waterlogged: " + targetBlock.getType());
                    event.setCancelled(true);
                    preserveBucket(player, item);
                    plugin.debugLog("Waterlogging complete, bucket preserved");
                    return;
                } else {
                    plugin.debugLog("Target block already waterlogged");
                }
            }
        }

        // Handle cauldron interactions
        if (clickedBlock != null && clickedBlock.getType() == Material.CAULDRON) {
            plugin.debugLog("Interacting with cauldron");
            if (bucketType == 0) {
                clickedBlock.setType(Material.WATER_CAULDRON);
                Levelled cauldronData = (Levelled) clickedBlock.getBlockData();
                cauldronData.setLevel(cauldronData.getMaximumLevel());
                clickedBlock.setBlockData(cauldronData);
                plugin.debugLog("Set cauldron to WATER_CAULDRON, level: " + cauldronData.getLevel());
            } else if (bucketType == 1) {
                clickedBlock.setType(Material.LAVA_CAULDRON);
                plugin.debugLog("Set cauldron to LAVA_CAULDRON");
            }
            event.setCancelled(true);
            preserveBucket(player, item);
            plugin.debugLog("Cauldron interaction complete, bucket preserved");
            return;
        }

        // Handle placement, including in its own element
        event.setCancelled(true);
        if (bucketType == 0) {
            if (isInOwnElement || targetBlock.getType().isAir() || targetBlock.getType() == Material.WATER || targetBlock.isPassable()) {
                plugin.debugLog("Placing water at target: " + targetBlock.getType());
                targetBlock.setType(Material.WATER);
                preserveBucket(player, item);
                plugin.debugLog("Water placement complete, bucket preserved");
            }
        } else if (bucketType == 1) {
            if (isInOwnElement || targetBlock.getType().isAir() || targetBlock.getType() == Material.LAVA || targetBlock.isPassable()) {
                plugin.debugLog("Placing lava at target: " + targetBlock.getType());
                targetBlock.setType(Material.LAVA);
                preserveBucket(player, item);
                plugin.debugLog("Lava placement complete, bucket preserved");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketDrain(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack bucket = event.getItemStack();

        plugin.debugLog("PlayerBucketEmptyEvent for " + player.getName() + ", bucket type: " + event.getBucket(), bucket);

        if (isInfinite(bucket)) {
            plugin.debugLog("Infinite bucket detected, cancelling and preserving bucket", bucket);
            event.setCancelled(true);
            preserveBucket(player, bucket);
            // Additional check to ensure the bucket is preserved
            player.getScheduler().runDelayed(plugin, scheduledTask -> {
                PlayerInventory inv = player.getInventory();
                ItemStack heldItem = inv.getItem(inv.getHeldItemSlot());
                if (!isInfinite(heldItem)) {
                    plugin.debugLog("Bucket missing after PlayerBucketEmptyEvent, restoring", bucket);
                    inv.setItem(inv.getHeldItemSlot(), bucket.clone());
                }
            }, null, 1L);
            return;
        }

        plugin.debugLog("Non-infinite bucket, allowing default behavior", bucket);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();

        plugin.debugLog("BlockPlaceEvent for block: " + block.getType(), item);

        if (isInfinite(item)) {
            plugin.debugLog("Infinite bucket detected, cancelling to prevent consumption", item);
            event.setCancelled(true);
        } else {
            plugin.debugLog("Non-infinite item, allowing default behavior", item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ClickType clickType = event.getClick();
        int slot = event.getSlot();

        boolean isCursorInfinite = isInfinite(cursor);
        boolean isCurrentInfinite = isInfinite(current);

        // Debug logs
        plugin.debugLog("InventoryClickEvent for " + player.getName() + ", inventory: " + event.getInventory().getType() +
                ", clicked: " + clickedInventory.getType() + ", click: " + clickType +
                ", slot: " + slot + ", current: " + (current != null ? current.getType() : "null") +
                ", cursor: " + cursor.getType());

        // Handle trade inventories (AxTrade GUI)
        boolean isTradeInventory = event.getInventory().getType() == InventoryType.MERCHANT ||
                event.getView().getTitle().toLowerCase().contains("trade");

        if (isTradeInventory && (isCurrentInfinite || isCursorInfinite)) {
            ItemStack bucket = isCurrentInfinite ? current : cursor;
            plugin.debugLog("Infinite bucket detected in trade inventory, click: " + clickType + ", slot: " + slot, bucket);

            // Prevent movement or removal of infinite bucket in trade slots
            if (isValidTradeSlot(slot)) {
                event.setCancelled(false);
                plugin.debugLog("Valid trade slot, allowing bucket interaction.");
            } else {
                event.setCancelled(true);
                plugin.debugLog("Invalid trade slot, cancelling interaction with infinite bucket.");
            }
            return;
        }

        // Handle infinite bucket interactions in normal inventories (chests, player inventory, etc.)
        if (!isCursorInfinite && !isCurrentInfinite) return;

        // Prevent stacking infinite buckets
        if (isCursorInfinite && isCurrentInfinite && current.isSimilar(cursor)) {
            plugin.debugLog("Attempt to stack infinite buckets, cancelling");
            event.setCancelled(true);
            return;
        }

        // Handle shift-clicking infinite buckets between player inventory and chest
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            if (isCurrentInfinite && event.getInventory().getType() == InventoryType.CHEST) {
                if (clickedInventory.getType() == InventoryType.PLAYER) {
                    // Shift-click from player inventory to "chest"
                    Inventory chestInventory = event.getInventory();
                    int emptySlot = chestInventory.firstEmpty();
                    if (emptySlot != -1) {
                        // Allow default behavior to move the bucket to the chest
                        plugin.debugLog("Allowing shift-click of infinite bucket to chest.");
                    } else {
                        // No space in "chest," cancel to prevent duplication
                        event.setCancelled(true);
                        plugin.debugLog("No space in chest, cancelling shift-click.");
                    }
                } else if (clickedInventory.getType() == InventoryType.CHEST) {
                    // Shift-click from chest to player inventory
                    // Allow default behavior if player inventory has space
                    PlayerInventory playerInventory = player.getInventory();
                    if (playerInventory.firstEmpty() != -1) {
                        plugin.debugLog("Allowing shift-click of infinite bucket to player inventory.");
                    } else {
                        // No space in player inventory, cancel to prevent issues
                        event.setCancelled(true);
                        plugin.debugLog("No space in player inventory, cancelling shift-click.");
                    }
                }
            }
        }
    }

    private void preserveBucket(Player player, ItemStack bucket) {
        PlayerInventory inventory = player.getInventory();
        int slot = inventory.getHeldItemSlot();
        ItemStack clonedBucket = bucket.clone(); // Clone to avoid reference issues
        inventory.setItem(slot, clonedBucket);
        // Schedule a task to ensure the bucket is preserved
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            PlayerInventory inv = player.getInventory();
            ItemStack heldItem = inv.getItem(inv.getHeldItemSlot());
            if (!isInfinite(heldItem)) {
                plugin.debugLog("Bucket missing after PlayerBucketEmptyEvent, restoring", bucket);
                inv.setItem(inv.getHeldItemSlot(), bucket.clone());
            }
        }, null, 1L);
        plugin.debugLog("Preserved bucket in slot " + slot, clonedBucket);
    }

    private boolean isInfinite(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(infiniteKey, PersistentDataType.INTEGER);
    }

    private boolean isValidTradeSlot(int slot) {
        return slot >= 0 && slot <= 8;
    }

    private int findValidTradeSlot(Inventory inventory) {
        // Find an empty slot in the trade offer area (slots 0-8)
        for (int i = 0; i <= 8; i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }
        return -1;
    }
}
