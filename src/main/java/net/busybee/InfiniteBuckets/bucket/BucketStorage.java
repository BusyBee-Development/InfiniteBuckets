package net.busybee.InfiniteBuckets.bucket;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.busybee.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class BucketStorage {
    private final Main plugin;

    public BucketStorage(Main plugin) {
        this.plugin = plugin;
    }

    public List<BucketTemplate> loadTemplates() {
        List<BucketTemplate> templates = new ArrayList<>();
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        ConfigurationSection section = config.getConfigurationSection("buckets");
        if (section == null) return templates;

        for (String id : section.getKeys(false)) {
            ConfigurationSection bucketSection = section.getConfigurationSection(id);
            if (bucketSection == null) continue;

            templates.add(BucketTemplate.builder()
                    .id(id)
                    .displayName(bucketSection.getString("display-name"))
                    .lore(bucketSection.getStringList("lore"))
                    .liquidType(Material.valueOf(bucketSection.getString("liquid-type", "WATER")))
                    .mode(BucketTemplate.BucketMode.valueOf(bucketSection.getString("mode", "VANILLA_LIKE")))
                    .usageLimit(bucketSection.getInt("usage-limit", -1))
                    .cooldown(bucketSection.getLong("cooldown", 0))
                    .permission(bucketSection.getString("permission"))
                    .glowing(bucketSection.getBoolean("glowing", false))
                    .customModelData(bucketSection.getInt("custom-model-data", 0))
                    .placeSound(XSound.matchXSound(bucketSection.getString("place-sound", "ITEM_BUCKET_EMPTY")).orElse(XSound.ITEM_BUCKET_EMPTY))
                    .refillSound(XSound.matchXSound(bucketSection.getString("refill-sound", "ITEM_BUCKET_FILL")).orElse(XSound.ITEM_BUCKET_FILL))
                    .icon(XMaterial.matchXMaterial(bucketSection.getString("icon", "WATER_BUCKET")).orElse(XMaterial.WATER_BUCKET))
                    .build());
        }
        return templates;
    }

    public void saveTemplate(BucketTemplate template) {
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        String path = "buckets." + template.getId() + ".";
        config.set(path + "display-name", template.getDisplayName());
        config.set(path + "lore", template.getLore());
        config.set(path + "liquid-type", template.getLiquidType().name());
        config.set(path + "mode", template.getMode() != null ? template.getMode().name() : BucketTemplate.BucketMode.VANILLA_LIKE.name());
        config.set(path + "usage-limit", template.getUsageLimit());
        config.set(path + "cooldown", template.getCooldown());
        config.set(path + "permission", template.getPermission());
        config.set(path + "glowing", template.isGlowing());
        config.set(path + "custom-model-data", template.getCustomModelData());
        config.set(path + "place-sound", template.getPlaceSound().name());
        config.set(path + "refill-sound", template.getRefillSound().name());
        config.set(path + "icon", template.getIcon().name());

        plugin.getConfigManager().saveBucketsConfig();
    }

    public void deleteTemplate(String id) {
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        config.set("buckets." + id, null);
        plugin.getConfigManager().saveBucketsConfig();
    }

    public void migrate() {
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        if (config.contains("buckets") || (!config.contains("presets") && !config.contains("customBuckets"))) return;

        plugin.getLogger().info("Migrating old buckets.yml format...");

        ConfigurationSection presets = config.getConfigurationSection("presets");
        if (presets != null) {
            for (String key : presets.getKeys(false)) {
                ConfigurationSection preset = presets.getConfigurationSection(key);
                if (preset == null) continue;

                String liquidName = preset.getStringList("fluids").stream().findFirst().orElse("WATER");
                if (liquidName.contains(":")) liquidName = liquidName.split(":")[1].toUpperCase();

                BucketTemplate template = BucketTemplate.builder()
                        .id(key)
                        .displayName(preset.getString("displayName", key))
                        .lore(preset.getStringList("lore"))
                        .liquidType(Material.matchMaterial(liquidName) != null ? Material.matchMaterial(liquidName) : Material.WATER)
                        .mode(BucketTemplate.BucketMode.valueOf(preset.getString("mode", "VANILLA_LIKE").toUpperCase()))
                        .usageLimit(preset.getInt("uses", -1))
                        .icon(XMaterial.matchXMaterial(preset.getString("icon", "WATER_BUCKET")).orElse(XMaterial.WATER_BUCKET))
                        .placeSound(XSound.ITEM_BUCKET_EMPTY)
                        .refillSound(XSound.ITEM_BUCKET_FILL)
                        .glowing(true)
                        .build();
                saveTemplate(template);
            }
        }

        config.set("presets", null);
        config.set("customBuckets", null);
        plugin.getConfigManager().saveBucketsConfig();
    }
}
