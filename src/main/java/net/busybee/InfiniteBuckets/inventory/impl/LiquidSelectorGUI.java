package net.busybee.InfiniteBuckets.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import fr.mrmicky.fastinv.FastInv;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.utils.GUIUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LiquidSelectorGUI extends FastInv {

    public LiquidSelectorGUI(BucketBuilderGUI parent) {
        this(parent, Main.getInstance().getConfigManager().getGuisConfig().getConfigurationSection("guis.liquid-selector"));
    }

    private LiquidSelectorGUI(BucketBuilderGUI parent, ConfigurationSection config) {
        super(config != null ? config.getInt("size", 27) : 27,
                Main.getInstance().getMessageManager().serialize(
                        Main.getInstance().getMessageManager().parse(config != null ? config.getString("title", "Select Liquid") : "Select Liquid")));

        Material[] liquids = {
                Material.WATER_BUCKET,
                Material.LAVA_BUCKET,
                Material.MILK_BUCKET,
                Material.POWDER_SNOW_BUCKET
        };

        for (int i = 0; i < liquids.length; i++) {
            Material liquid = liquids[i];
            setItem(i, GUIUtils.createItem(liquid, "<aqua>" + liquid.name(), new ArrayList<>()), e -> {
                Material type = liquid == Material.WATER_BUCKET ? Material.WATER :
                               (liquid == Material.LAVA_BUCKET ? Material.LAVA :
                               (liquid == Material.POWDER_SNOW_BUCKET ? Material.POWDER_SNOW : Material.MILK_BUCKET));
                parent.getTemplate().setLiquidType(type);
                parent.getTemplate().setIcon(XMaterial.matchXMaterial(liquid));
                parent.refresh();
                parent.open((Player) e.getWhoClicked());
            });
        }
    }
}
