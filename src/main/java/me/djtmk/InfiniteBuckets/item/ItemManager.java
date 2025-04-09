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

    public ItemManager(Main plugin) {
        this.plugin = plugin;
    }

    public ItemStack infiniteWaterBucket() {
        return createInfiniteBucket(Material.WATER_BUCKET, "water.display", "water.lore", 0);
    }

    public ItemStack infiniteLavaBucket() {
        return createInfiniteBucket(Material.LAVA_BUCKET, "lava.display", "lava.lore", 1);
    }

    private ItemStack createInfiniteBucket(Material material, String displayKey, String loreKey, int persistentValue) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Failed to get ItemMeta for material: " + material);
        }

        String displayName = plugin.getConfig().getString(displayKey, ChatColor.RESET + material.name());
        List<String> lore = plugin.getConfig().getStringList(loreKey).stream()
                .map(StringUtils::format)
                .collect(Collectors.toList());
        if (lore.isEmpty()) {
            lore.add(ChatColor.GRAY + "Right-click to use this bucket!");
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