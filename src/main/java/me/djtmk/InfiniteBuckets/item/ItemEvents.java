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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemEvents implements Listener {

    private final Main plugin;

    public ItemEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBucketDrain(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack bucket = player.getInventory().getItemInMainHand();

        // Check if the item has metadata
        if (!bucket.hasItemMeta()) return;

        NamespacedKey infinite = new NamespacedKey(plugin, "infinite");
        PersistentDataContainer container = bucket.getItemMeta().getPersistentDataContainer();

        // Check if it's an infinite bucket
        if (!container.has(infinite, PersistentDataType.INTEGER)) return;

        Block clickedBlock = event.getBlockClicked();
        BlockFace face = event.getBlockFace();
        Block targetBlock = clickedBlock.getRelative(face);

        Integer bucketType = container.get(infinite, PersistentDataType.INTEGER);

        // Handle cauldron filling
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

        // Place liquid in the adjacent block
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
}