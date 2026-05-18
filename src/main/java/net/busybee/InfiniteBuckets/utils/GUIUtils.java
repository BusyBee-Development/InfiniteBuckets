package net.busybee.InfiniteBuckets.utils;

import com.cryptomorin.xseries.XMaterial;
import net.busybee.InfiniteBuckets.Main;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class GUIUtils {

    public static ItemStack createItem(ConfigurationSection section, TagResolver... placeholders) {
        if (section == null) return new ItemStack(Material.AIR);

        String materialStr = section.getString("material", "BARRIER");
        XMaterial xMaterial = XMaterial.matchXMaterial(materialStr).orElse(XMaterial.BARRIER);
        ItemStack item = xMaterial.parseItem();
        if (item == null) item = new ItemStack(Material.BARRIER);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = section.getString("name");
            if (name != null) {
                meta.displayName(Main.getInstance().getMessageManager().parse(name, placeholders));
            }

            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.lore(Main.getInstance().getMessageManager().parse(lore, placeholders));
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore, TagResolver... placeholders) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Main.getInstance().getMessageManager().parse(name, placeholders));
            meta.lore(Main.getInstance().getMessageManager().parse(lore, placeholders));
            item.setItemMeta(meta);
        }
        return item;
    }
}
