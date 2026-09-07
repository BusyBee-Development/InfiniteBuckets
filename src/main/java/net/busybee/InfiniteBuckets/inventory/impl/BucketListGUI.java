package net.busybee.InfiniteBuckets.inventory.impl;

import fr.mrmicky.fastinv.FastInv;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.utils.GUIUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BucketListGUI extends FastInv {

    public BucketListGUI() {
        this(0);
    }

    public BucketListGUI(int page) {
        this(page, Main.getInstance().getConfigManager().getGuisConfig().getConfigurationSection("guis.list"));
    }

    private BucketListGUI(int page, ConfigurationSection config) {
        super(config != null ? config.getInt("size", 54) : 54,
                Main.getInstance().getMessageManager().serialize(
                        Main.getInstance().getMessageManager().parse(
                                config != null ? config.getString("title", "Infinite Buckets") : "Infinite Buckets",
                                Placeholder.parsed("page", String.valueOf(page + 1)))));

        if (config == null) return;

        ConfigurationSection fillerSection = config.getConfigurationSection("filler");
        if (fillerSection != null) {
            ItemStack filler = GUIUtils.createItem(fillerSection);
            for (int i = 0; i < getInventory().getSize(); i++) {
                setItem(i, filler);
            }
        }

        List<BucketTemplate> templates = new ArrayList<>(Main.getInstance().getBucketRegistry().getRegisteredTemplates());
        int start = page * 45;
        int end = Math.min(start + 45, templates.size());

        ConfigurationSection bucketItemSec = config.getConfigurationSection("bucket-item");
        List<String> loreSuffix = bucketItemSec != null ? bucketItemSec.getStringList("lore-suffix") : new ArrayList<>();

        for (int i = start; i < end; i++) {
            BucketTemplate template = templates.get(i);
            ItemStack item = BucketFactory.createBucket(template);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<net.kyori.adventure.text.Component> lore = meta.lore();
                if (lore == null) lore = new ArrayList<>();
                lore.addAll(Main.getInstance().getMessageManager().parse(loreSuffix));
                meta.lore(lore);
                item.setItemMeta(meta);
            }

            setItem(i - start, item, e -> {
                Player player = (Player) e.getWhoClicked();
                if (e.isShiftClick()) {
                    player.getInventory().addItem(BucketFactory.createBucket(template));
                } else if (e.isRightClick()) {
                    new ConfirmDeleteGUI(template).open(player);
                } else {
                    new BucketBuilderGUI(template).open(player);
                }
            });
        }

        ConfigurationSection createSec = config.getConfigurationSection("create-item");
        if (createSec != null) {
            setItem(createSec.getInt("slot"), GUIUtils.createItem(createSec), e -> new BucketBuilderGUI(null).open((Player) e.getWhoClicked()));
        }
    }
}
