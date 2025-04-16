package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemEvents implements Listener {

    private final Main plugin;
    private final NamespacedKey infiniteKey;

    public ItemEvents(Main plugin) {
        this.plugin = plugin;
        this.infiniteKey = new NamespacedKey(plugin, "infinite");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block clickedBlock = event.getClickedBlock();
        plugin.debugLog("PlayerInteractEvent for " + player.getName() + ", block: " + (clickedBlock != null ? clickedBlock.getType() : "null"), item);

        if (item == null || (item.getType() != Material.WATER_BUCKET && item.getType() != Material.LAVA_BUCKET)) {
            plugin.debugLog("Not a bucket item");
            return;
        }

        if (!isInfinite(item)) {
            plugin.debugLog("Bucket is not infinite", item);
            return;
        }

        plugin.debugLog("Infinite bucket detected. Type: " + item.getType());

        if (clickedBlock == null) {
            plugin.debugLog("No block clicked");
            return;
        }

        Integer bucketType = item.getItemMeta().getPersistentDataContainer().get(infiniteKey, PersistentDataType.INTEGER);
        plugin.debugLog("Bucket infinite type: " + (bucketType == 0 ? "Water" : "Lava"));

        BlockFace face = event.getBlockFace();
        Block targetBlock = clickedBlock.getRelative(face);
        plugin.debugLog("Target block: " + targetBlock.getType());

        // Handle waterlogging for water buckets
        if (bucketType == 0) {
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
        if (clickedBlock.getType() == Material.CAULDRON) {
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

        // Handle placement in air or replaceable blocks
        if (bucketType == 0) {
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.WATER) {
                plugin.debugLog("Placing water at target: " + targetBlock.getType());
                targetBlock.setType(Material.WATER);
                event.setCancelled(true);
                preserveBucket(player, item);
                plugin.debugLog("Water placement complete, bucket preserved");
            }
        } else if (bucketType == 1) {
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.LAVA) {
                plugin.debugLog("Placing lava at target: " + targetBlock.getType());
                targetBlock.setType(Material.LAVA);
                event.setCancelled(true);
                preserveBucket(player, item);
                plugin.debugLog("Lava placement complete, bucket preserved");
            }
        }
    }

    @EventHandler
    public void onBucketDrain(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack bucket = event.getItemStack();

        plugin.debugLog("PlayerBucketEmptyEvent for " + player.getName() + ", bucket type: " + event.getBucket(), bucket);

        if (isInfinite(bucket)) {
            plugin.debugLog("Infinite bucket detected, cancelling to defer to PlayerInteractEvent", bucket);
            event.setCancelled(true);
            return;
        }

        plugin.debugLog("Non-infinite bucket, allowing default behavior", bucket);
    }

    @EventHandler
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

        boolean isCursorInfinite = isInfinite(cursor);
        boolean isCurrentInfinite = isInfinite(current);

        plugin.debugLog("InventoryClickEvent for " + player.getName() + ", inventory: " + event.getInventory().getType() +
                ", clicked: " + (clickedInventory.getType()) + ", current: " + (current != null ? current.getType() : "null") +
                ", cursor: " + (cursor != null ? cursor.getType() : "null"));

        // Handle trade inventories
        if (event.getInventory().getType() == InventoryType.MERCHANT ||
                event.getView().getTitle().toLowerCase().contains("trade")) {
            if (isCurrentInfinite || isCursorInfinite) {
                plugin.debugLog("Infinite bucket detected in trade inventory, cancelling click", isCurrentInfinite ? current : cursor);
                event.setCancelled(true);
                return;
            }
        }

        if (!isCursorInfinite && !isCurrentInfinite) return;

        if (isCursorInfinite && isCurrentInfinite && current.isSimilar(cursor)) {
            plugin.debugLog("Attempt to stack infinite buckets, cancelling");
            event.setCancelled(true);
            return;
        }

        if (isCursorInfinite && clickedInventory.getType() == InventoryType.CHEST) {
            ItemStack existingItem = clickedInventory.getItem(event.getSlot());
            if (isInfinite(existingItem)) {
                plugin.debugLog("Infinite bucket already in chest slot, cancelling");
                event.setCancelled(true);
                return;
            }
        }

        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) &&
                isCurrentInfinite && event.getInventory().getType() == InventoryType.CHEST &&
                clickedInventory.getType() == InventoryType.PLAYER) {

            Inventory chest = event.getInventory();
            ItemStack itemToMove = current.clone();
            itemToMove.setAmount(1);

            int emptySlot = chest.firstEmpty();
            if (emptySlot != -1) {
                chest.setItem(emptySlot, itemToMove);
                clickedInventory.setItem(event.getSlot(), null);
                player.updateInventory();
                plugin.debugLog("Moved infinite bucket to chest, cleared player slot");
            }
            event.setCancelled(true);
        }

        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) &&
                isCurrentInfinite && clickedInventory.getType() == InventoryType.CHEST &&
                event.getInventory().getType() == InventoryType.PLAYER) {

            event.setCancelled(true);

            final ItemStack movedItem = current.clone();
            movedItem.setAmount(1);
            final int sourceSlot = event.getSlot();

            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory playerInv = player.getInventory();
                    int emptySlot = playerInv.firstEmpty();
                    if (emptySlot != -1) {
                        playerInv.setItem(emptySlot, movedItem);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), movedItem);
                    }

                    ItemStack chestItem = clickedInventory.getItem(sourceSlot);
                    if (chestItem != null) {
                        if (chestItem.getAmount() <= 1) {
                            clickedInventory.setItem(sourceSlot, null);
                        } else {
                            chestItem.setAmount(chestItem.getAmount() - 1);
                        }
                    }

                    player.updateInventory();
                    plugin.debugLog("Moved infinite bucket to player inventory, updated chest");
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        // Check if closing a trade inventory
        if (inventory.getType() == InventoryType.MERCHANT ||
                event.getView().getTitle().toLowerCase().contains("trade")) {
            plugin.debugLog("InventoryCloseEvent for " + player.getName() + ", inventory: " + inventory.getType() +
                    ", title: " + event.getView().getTitle());

            // Delayed check to ensure trade is complete
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerInventory playerInv = player.getInventory();
                    boolean foundInfinite = false;
                    int infiniteSlot = -1;

                    // Check for infinite buckets in player's inventory
                    for (int i = 0; i < playerInv.getSize(); i++) {
                        ItemStack item = playerInv.getItem(i);
                        if (isInfinite(item)) {
                            foundInfinite = true;
                            infiniteSlot = i;
                            plugin.debugLog("Found infinite bucket in " + player.getName() + "'s inventory after trade, slot: " + i, item);
                            break;
                        }
                    }

                    // If an infinite bucket remains, warn and remove (assuming trade completed)
                    if (foundInfinite) {
                        plugin.debugLog("Potential duplication detected for " + player.getName() + ", removing infinite bucket post-trade");
                        playerInv.setItem(infiniteSlot, null);
                        player.updateInventory();
                        player.sendMessage("§cWarning: Infinite bucket was removed to prevent duplication after trading.");
                    }
                }
            }.runTaskLater(plugin, 2L); // Delay to allow trade to finalize
        }
    }

    private void preserveBucket(Player player, ItemStack bucket) {
        PlayerInventory inventory = player.getInventory();
        int slot = inventory.getHeldItemSlot();
        inventory.setItem(slot, bucket);
        player.updateInventory();
        plugin.debugLog("Synchronously preserved bucket in slot " + slot, bucket);
    }

    private boolean isInfinite(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(infiniteKey, PersistentDataType.INTEGER);
    }
}