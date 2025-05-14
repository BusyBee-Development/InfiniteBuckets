package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {

    private final Main plugin;
    private final String waterDisplay;
    private final List<String> waterLore;
    private final String lavaDisplay;
    private final List<String> lavaLore;

    public ItemManager(Main plugin) {
        this.plugin = plugin;
        this.waterDisplay = plugin.getConfig().getString("water.display", ChatColor.RESET + "Infinite Water Bucket");
        this.waterLore = plugin.getConfig().getStringList("water.lore").stream()
                .map(StringUtils::format)
                .collect(Collectors.toList());
        if (waterLore.isEmpty()) {
            waterLore.add(ChatColor.GRAY + "Right-click to use this bucket!");
        }
        this.lavaDisplay = plugin.getConfig().getString("lava.display", ChatColor.RESET + "Infinite Lava Bucket");
        this.lavaLore = plugin.getConfig().getStringList("lava.lore").stream()
                .map(StringUtils::format)
                .collect(Collectors.toList());
        if (lavaLore.isEmpty()) {
            lavaLore.add(ChatColor.GRAY + "Right-click to use this bucket!");
        }
    }

    public ItemStack infiniteWaterBucket() {
        return createInfiniteBucket(Material.WATER_BUCKET, waterDisplay, waterLore, 0);
    }

    public ItemStack infiniteLavaBucket() {
        return createInfiniteBucket(Material.LAVA_BUCKET, lavaDisplay, lavaLore, 1);
    }

    private ItemStack createInfiniteBucket(Material material, String displayName, List<String> lore, int persistentValue) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Failed to get ItemMeta for material: " + material);
        }

        meta.setDisplayName(StringUtils.format(displayName));
        meta.setLore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 3, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        NamespacedKey infiniteKey = new NamespacedKey(plugin, "infinite");
        meta.getPersistentDataContainer().set(infiniteKey, PersistentDataType.INTEGER, persistentValue);

        item.setItemMeta(meta);
        return item;
    }
}
