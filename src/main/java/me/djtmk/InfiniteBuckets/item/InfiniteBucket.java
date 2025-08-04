package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record InfiniteBucket(
        String id,
        Material material,
        Component displayName,
        List<Component> lore,
        String permission,
        boolean worksInNether
) {
    public ItemStack createItem(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "infinite_bucket");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);

        meta.displayName(this.displayName);
        meta.lore(this.lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    public static Optional<InfiniteBucket> fromConfig(Main plugin, String id, ConfigurationSection config) {
        MiniMessage mm = MiniMessage.miniMessage();

        String materialName = config.getString("material");
        if (materialName == null || materialName.isBlank()) {
            plugin.getLogger().warning("Bucket '" + id + "' in config.yml is missing the 'material' key. Skipping.");
            return Optional.empty();
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Bucket '" + id + "' has an invalid material: '" + materialName + "'. Skipping.");
            return Optional.empty();
        }

        if (!material.name().endsWith("_BUCKET")) {
            plugin.getLogger().warning("Material '" + material.name() + "' for bucket '" + id + "' is not a valid bucket type. Skipping.");
            return Optional.empty();
        }

        Component displayName = mm.deserialize(config.getString("display-name", "<red>Invalid Bucket</red>"));
        List<Component> lore = config.getStringList("lore").stream().map(mm::deserialize).collect(Collectors.toList());
        String permission = config.getString("permission", "infb.use." + id);
        boolean worksInNether = config.getBoolean("works-in-nether", true);

        return Optional.of(new InfiniteBucket(id, material, displayName, lore, permission, worksInNether));
    }
}
