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
        startStackingMonitor(); // Start the monitoring task
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

        if (bucketType == 0) { // Infinite water
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.WATER) {
                targetBlock.setType(Material.WATER);
                event.setCancelled(true);
            }
        } else if (bucketType == 1) { // Infinite lava
            if (targetBlock.getType().isAir() || targetBlock.getType() == Material.LAVA) {
                targetBlock.setType(Material.LAVA);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null || (cursor == null && current == null)) return;

        Player player = (Player) event.getWhoClicked();

        // Check if the current item or cursor is an infinite bucket
        boolean isCursorInfinite = cursor != null && cursor.hasItemMeta() &&
                cursor.getItemMeta().getPersistentDataContainer().has(infiniteKey, PersistentDataType.INTEGER);
        boolean isCurrentInfinite = current != null && current.hasItemMeta() &&
                current.getItemMeta().getPersistentDataContainer().has(infiniteKey, PersistentDataType.INTEGER);

        if (!isCursorInfinite && !isCurrentInfinite) return;

        // Prevent stacking when dragging an infinite bucket onto another
        if (isCursorInfinite && isCurrentInfinite && current.isSimilar(cursor)) {
            event.setCancelled(true);
            return;
        }

        // Handle placing an infinite bucket into a chest (manual click)
        if (isCursorInfinite && clickedInventory.getType() == InventoryType.CHEST) {
            int slot = event.getSlot();
            ItemStack existingItem = clickedInventory.getItem(slot);

            if (existingItem != null && existingItem.hasItemMeta() &&
                    existingItem.getItemMeta().getPersistentDataContainer().has(infiniteKey, PersistentDataType.INTEGER)) {
                event.setCancelled(true);
                return;
            }
        }

        // Handle shift-click from player inventory to chest
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

        // Handle shift-click from chest to player inventory
        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) &&
                isCurrentInfinite && clickedInventory.getType() == InventoryType.CHEST &&
                event.getInventory().getType() == InventoryType.PLAYER) {
            // Let Minecraft handle the transfer; the monitor will fix stacking
        }
    }

    private void startStackingMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Inventory playerInventory = player.getInventory();
                    for (int i = 0; i < playerInventory.getSize(); i++) {
                        ItemStack item = playerInventory.getItem(i);
                        if (item != null && item.getAmount() > 1 && item.hasItemMeta() &&
                                item.getItemMeta().getPersistentDataContainer().has(infiniteKey, PersistentDataType.INTEGER)) {
                            plugin.getLogger().info("Stacking detected: " + item.getType() + " x" + item.getAmount() + " at slot " + i + " for " + player.getName());
                            int excess = item.getAmount() - 1;
                            item.setAmount(1);
                            for (int j = 0; j < excess; j++) {
                                ItemStack singleItem = item.clone();
                                singleItem.setAmount(1);
                                int emptySlot = playerInventory.firstEmpty();
                                if (emptySlot != -1) {
                                    playerInventory.setItem(emptySlot, singleItem);
                                } else {
                                    player.getWorld().dropItemNaturally(player.getLocation(), singleItem);
                                }
                            }
                            player.updateInventory();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Run every 5 ticks (0.25 seconds)
    }
}