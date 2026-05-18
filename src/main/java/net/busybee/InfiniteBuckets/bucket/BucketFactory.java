package net.busybee.InfiniteBuckets.bucket;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class BucketFactory {
    public static final NamespacedKey BUCKET_ID_KEY = new NamespacedKey(Main.getInstance(), "bucket_id");
    public static final NamespacedKey USES_REMAINING_KEY = new NamespacedKey(Main.getInstance(), "uses_remaining");

    public static ItemStack createBucket(BucketTemplate template) {
        ItemStack item = template.getIcon().parseItem();
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        MiniMessage mm = MiniMessage.miniMessage();
        try {
            if (template.getDisplayName() != null) {
                meta.displayName(mm.deserialize(MessageManager.legacyToMiniMessage(template.getDisplayName())));
            }

            if (template.getLore() != null) {
                List<Component> lore = template.getLore().stream()
                        .map(line -> mm.deserialize(MessageManager.legacyToMiniMessage(line)))
                        .collect(Collectors.toList());
                meta.lore(lore);
            }
        } catch (Exception e) {
            if (template.getDisplayName() != null) {
                meta.displayName(Component.text(template.getDisplayName()));
            }
        }

        meta.getPersistentDataContainer().set(BUCKET_ID_KEY, PersistentDataType.STRING, template.getId());
        if (template.getUsageLimit() > 0) {
            meta.getPersistentDataContainer().set(USES_REMAINING_KEY, PersistentDataType.INTEGER, template.getUsageLimit());
        }

        if (template.getCustomModelData() != 0) {
            meta.setCustomModelData(template.getCustomModelData());
        }

        if (template.isGlowing()) {
            meta.setEnchantmentGlintOverride(true);
        }

        item.setItemMeta(meta);
        return item;
    }
}
