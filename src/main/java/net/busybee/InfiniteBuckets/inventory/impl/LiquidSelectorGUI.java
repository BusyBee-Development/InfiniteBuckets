package net.busybee.InfiniteBuckets.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import fr.mrmicky.fastinv.FastInv;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.utils.GUIUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LiquidSelectorGUI extends FastInv {

    public LiquidSelectorGUI(BucketBuilderGUI parent) {
        this(parent, Main.getInstance().getConfigManager().getGuisConfig().getConfigurationSection("guis.liquid-selector"));
    }

    private LiquidSelectorGUI(BucketBuilderGUI parent, ConfigurationSection config) {
        super(config != null ? config.getInt("size", 27) : 27,
                Main.getInstance().getMessageManager().serialize(
                        Main.getInstance().getMessageManager().parse(config != null ? config.getString("title", "Select Liquid") : "Select Liquid")));

        int slot = 0;
        for (Material liquid : BucketTemplate.PLACEABLE_LIQUIDS) {
            Material icon = BucketTemplate.bucketIcon(liquid);
            setItem(slot++, GUIUtils.createItem(icon, "<aqua>" + liquid.name(), new ArrayList<>()), e -> {
                parent.getTemplate().setLiquidType(liquid);
                parent.getTemplate().setIcon(XMaterial.matchXMaterial(icon));
                parent.refresh();
                parent.open((Player) e.getWhoClicked());
            });
        }
    }
}
