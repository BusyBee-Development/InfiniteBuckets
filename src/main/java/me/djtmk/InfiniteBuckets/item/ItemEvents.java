package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemEvents implements Listener {

    private final Main plugin;
    private final NamespacedKey infiniteKey;

    public ItemEvents(Main plugin) {
        this.plugin = plugin;
        this.infiniteKey = new NamespacedKey(plugin, "infinite");
        startStackingMonitor(); // Start the monitoring task to prevent stacking
    }

    @EventHandler
    public void onBucketDrain(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack bucket = player.getInventory().getItemInMainHand();

        if (!bucket.hasItemMeta()) return;

        PersistentDataContainer container = bucket.getItemMeta().getPersistentDataContainer();
        if (!container.has(infiniteKey, PersistentDataType.INTEGER)) return;

        Block clickedBlock = event.getBlockClicked();
        BlockFace face = event.getBlockFace();
        Block targetBlock = clickedBlock.getRelative(face);

        Integer bucketType = container.get(infiniteKey, PersistentDataType.INTEGER);

        if (clickedBlock.getType() == Material.CAULDRON) {
            if (bucketType == 0) { // Water
                clickedBlock.setType(Material.WATER_CAULDRON);
                Levelled cauldronData = (Levelled) clickedBlock.getBlockData();
                cauldronData.setLevel(cauldronData.getMaximumLevel());
                clickedBlock.setBlockData(cauldronData);
                event.setCancelled(true);
            } else if (bucketType == 1) { // Lava
                clickedBlock.setType(Material.LAVA_CAULDRON);
                event.setCancelled(true);
            }
            return;
        }

        if (bucketType == 0) {
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.WATER) {
                targetBlock.setType(Material.WATER);
                event.setCancelled(true);
            }
        } else if (bucketType == 1) {
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.LAVA) {
                targetBlock.setType(Material.LAVA);
                event.setCancelled(true);
            }
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

        if (!isCursorInfinite && !isCurrentInfinite) return;

        // Prevent stacking by dragging (same logic for water and lava)
        if (isCursorInfinite && isCurrentInfinite && current.isSimilar(cursor)) {
            event.setCancelled(true);
            return;
        }

        // Prevent placing into chest manually if it's already in there
        if (isCursorInfinite && clickedInventory.getType() == InventoryType.CHEST) {
            ItemStack existingItem = clickedInventory.getItem(event.getSlot());
            if (isInfinite(existingItem)) {
                event.setCancelled(true);
                return;
            }
        }

        // Shift-click from player -> chest (no stacking)
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
            }
            event.setCancelled(true);
        }

        // Fix: Shift-click from chest -> player (no stacking, direct move)
        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) &&
                isCurrentInfinite && clickedInventory.getType() == InventoryType.CHEST &&
                event.getInventory().getType() == InventoryType.PLAYER) {

            event.setCancelled(true); // Cancel default behavior

            final ItemStack movedItem = current.clone();
            movedItem.setAmount(1); // Only move a single item

            final int sourceSlot = event.getSlot();

            // New: Prevent stacking when moving infinite lava buckets from chest to player inventory
            if (isCurrentInfinite) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Inventory playerInv = player.getInventory();

                        // Check if this is an infinite bucket and directly place in the next available slot
                        int emptySlot = playerInv.firstEmpty();
                        if (emptySlot != -1) {
                            playerInv.setItem(emptySlot, movedItem);
                        } else {
                            // If no empty slot, drop the item naturally
                            player.getWorld().dropItemNaturally(player.getLocation(), movedItem);
                        }

                        // Remove the original bucket from the chest slot
                        ItemStack chestItem = clickedInventory.getItem(sourceSlot);
                        if (chestItem != null) {
                            if (chestItem.getAmount() <= 1) {
                                clickedInventory.setItem(sourceSlot, null);
                            } else {
                                chestItem.setAmount(chestItem.getAmount() - 1);
                            }
                        }

                        player.updateInventory(); // Update player inventory to reflect the change
                    }
                }.runTaskLater(plugin, 1L); // Run after 1 tick to ensure the transition is smooth
            }
        }
    }

    private boolean isInfinite(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(infiniteKey, PersistentDataType.INTEGER);
    }

    private void startStackingMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Inventory inv = player.getInventory();
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getAmount() > 1 && isInfinite(item)) {
                            int excess = item.getAmount() - 1;
                            item.setAmount(1);
                            for (int j = 0; j < excess; j++) {
                                ItemStack single = item.clone();
                                single.setAmount(1);
                                int empty = inv.firstEmpty();
                                if (empty != -1) {
                                    inv.setItem(empty, single);
                                } else {
                                    player.getWorld().dropItemNaturally(player.getLocation(), single);
                                }
                            }
                            player.updateInventory();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Monitor every 5 ticks
    }
}
