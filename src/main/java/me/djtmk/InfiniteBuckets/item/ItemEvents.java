package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();

        ItemStack bucket = player.getInventory().getItemInMainHand();

        // no meta
        if (!bucket.hasItemMeta()) return;

        NamespacedKey infinite = new NamespacedKey(plugin, "infinite");
        PersistentDataContainer container = bucket.getItemMeta().getPersistentDataContainer();

        // not an infinite bucket
        if (!container.has(infinite, PersistentDataType.INTEGER)) return;

        // infinite water
        if (container.get(infinite, PersistentDataType.INTEGER) == 0) {
            player.getWorld().getBlockAt(x, y, z).setType(Material.WATER);
            event.setCancelled(true);
        }

        // infinite lava
        if (container.get(infinite, PersistentDataType.INTEGER) == 1) {
            player.getWorld().getBlockAt(x, y, z).setType(Material.LAVA);
            event.setCancelled(true);
        }
    }
}
