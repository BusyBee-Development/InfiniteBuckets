package net.busybee.InfiniteBuckets.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastinv.FastInv;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.utils.GUIUtils;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BucketBuilderGUI extends FastInv {
    private final BucketTemplate template;
    private final ConfigurationSection config;

    public BucketBuilderGUI(BucketTemplate existing) {
        this(existing, Main.getInstance().getConfigManager().getGuisConfig().getConfigurationSection("guis.builder"));
    }

    private BucketBuilderGUI(BucketTemplate existing, ConfigurationSection config) {
        super(config != null ? config.getInt("size", 54) : 54,
                Main.getInstance().getMessageManager().serialize(
                        Main.getInstance().getMessageManager().parse(config != null ? config.getString("title", "Bucket Builder") : "Bucket Builder")));
        this.config = config;

        if (existing == null) {
            this.template = BucketTemplate.builder()
                    .id("bucket_" + (Main.getInstance().getBucketRegistry().getRegisteredTemplates().size() + 1))
                    .displayName("<aqua>Infinite Bucket</aqua>")
                    .lore(new ArrayList<>(List.of("<gray>Never runs out!</gray>")))
                    .liquidType(Material.WATER)
                    .mode(BucketTemplate.BucketMode.VANILLA_LIKE)
                    .usageLimit(-1)
                    .cooldown(0)
                    .icon(XMaterial.WATER_BUCKET)
                    .placeSound(XSound.ITEM_BUCKET_EMPTY)
                    .refillSound(XSound.ITEM_BUCKET_FILL)
                    .glowing(true)
                    .build();
        } else {
            this.template = existing;
        }

        refresh();
    }

    public void refresh() {
        if (config == null) return;

        // Fill background
        ConfigurationSection fillerSection = config.getConfigurationSection("filler");
        if (fillerSection != null) {
            ItemStack filler = GUIUtils.createItem(fillerSection);
            for (int i = 0; i < getInventory().getSize(); i++) {
                setItem(i, filler);
            }
        }

        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) return;

        // Liquid Selector
        ConfigurationSection liquidSec = items.getConfigurationSection("liquid-selector");
        if (liquidSec != null) {
            Material liquidIcon = template.getLiquidType() == Material.LAVA ? Material.LAVA_BUCKET :
                    (template.getLiquidType() == Material.POWDER_SNOW ? Material.POWDER_SNOW_BUCKET :
                     (template.getLiquidType() == Material.MILK_BUCKET ? Material.MILK_BUCKET : Material.WATER_BUCKET));
            setItem(liquidSec.getInt("slot"), GUIUtils.createItem(liquidIcon, liquidSec.getString("name"), liquidSec.getStringList("lore"),
                    Placeholder.parsed("liquid", template.getLiquidType().name())),
                    e -> new LiquidSelectorGUI(this).open((Player) e.getWhoClicked()));
        }

        // Edit Name
        ConfigurationSection nameSec = items.getConfigurationSection("edit-name");
        if (nameSec != null) {
            setItem(nameSec.getInt("slot"), GUIUtils.createItem(nameSec, Placeholder.parsed("name", template.getDisplayName())),
                    e -> {
                        e.getWhoClicked().closeInventory();
                        ChatPromptListener.startPrompt((Player) e.getWhoClicked(), template, ChatPromptListener.PromptType.NAME);
                    });
        }

        // Edit Lore
        ConfigurationSection loreSec = items.getConfigurationSection("edit-lore");
        if (loreSec != null) {
            setItem(loreSec.getInt("slot"), GUIUtils.createItem(loreSec),
                    e -> {
                        e.getWhoClicked().closeInventory();
                        ChatPromptListener.startPrompt((Player) e.getWhoClicked(), template, ChatPromptListener.PromptType.LORE);
                    });
        }

        // Usage Limit
        ConfigurationSection limitSec = items.getConfigurationSection("usage-limit");
        if (limitSec != null) {
            String limitStr = template.getUsageLimit() == -1 ? "Infinite" : String.valueOf(template.getUsageLimit());
            setItem(limitSec.getInt("slot"), GUIUtils.createItem(limitSec, Placeholder.parsed("limit", limitStr)),
                    e -> {
                        if (template.getUsageLimit() == -1) template.setUsageLimit(100);
                        else if (template.getUsageLimit() == 100) template.setUsageLimit(500);
                        else if (template.getUsageLimit() == 500) template.setUsageLimit(-1);
                        refresh();
                    });
        }

        // Glowing
        ConfigurationSection glowSec = items.getConfigurationSection("glowing");
        if (glowSec != null) {
            String status = template.isGlowing() ? "<green>Enabled</green>" : "<red>Disabled</red>";
            setItem(glowSec.getInt("slot"), GUIUtils.createItem(glowSec, Placeholder.parsed("status", status)),
                    e -> {
                        template.setGlowing(!template.isGlowing());
                        refresh();
                    });
        }

        // Save
        ConfigurationSection saveSec = items.getConfigurationSection("save");
        if (saveSec != null) {
            setItem(saveSec.getInt("slot"), GUIUtils.createItem(saveSec), e -> {
                Main.getInstance().getBucketRegistry().registerTemplate(template);
                Main.getInstance().getMessageManager().send(e.getWhoClicked(), "gui.status.saved");
                new BucketListGUI().open((Player) e.getWhoClicked());
            });
        }

        // Cancel
        ConfigurationSection cancelSec = items.getConfigurationSection("cancel");
        if (cancelSec != null) {
            setItem(cancelSec.getInt("slot"), GUIUtils.createItem(cancelSec), e -> new BucketListGUI().open((Player) e.getWhoClicked()));
        }
    }

    public BucketTemplate getTemplate() {
        return template;
    }
}
