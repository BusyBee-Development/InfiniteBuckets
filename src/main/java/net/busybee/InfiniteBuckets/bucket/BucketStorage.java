package net.busybee.InfiniteBuckets.bucket;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.busybee.InfiniteBuckets.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

            String liquidName = bucketSection.getString("liquid-type", "WATER");
            Material liquid = Material.matchMaterial(liquidName);
            if (liquid == null || !BucketTemplate.PLACEABLE_LIQUIDS.contains(liquid)) {
                plugin.getLogger().warning("Skipping bucket '" + id + "': liquid-type " + liquidName
                        + " can't be placed. Use one of " + BucketTemplate.PLACEABLE_LIQUIDS + ".");
                continue;
            }

            BucketTemplate.BucketMode mode;
            try {
                mode = BucketTemplate.BucketMode.valueOf(bucketSection.getString("mode", "VANILLA_LIKE").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Bucket '" + id + "' has an unknown mode; using VANILLA_LIKE.");
                mode = BucketTemplate.BucketMode.VANILLA_LIKE;
            }

            templates.add(BucketTemplate.builder()
                    .id(id)
                    .displayName(bucketSection.getString("display-name", id))
                    .lore(bucketSection.getStringList("lore"))
                    .liquidType(liquid)
                    .mode(mode)
                    .usageLimit(bucketSection.getInt("usage-limit", -1))
                    .cooldown(bucketSection.getLong("cooldown", 0))
                    .permission(bucketSection.getString("permission"))
                    .glowing(bucketSection.getBoolean("glowing", false))
                    .customModelData(bucketSection.getInt("custom-model-data", 0))
                    .placeSound(XSound.matchXSound(bucketSection.getString("place-sound", "ITEM_BUCKET_EMPTY")).orElse(XSound.ITEM_BUCKET_EMPTY))
                    .refillSound(XSound.matchXSound(bucketSection.getString("refill-sound", "ITEM_BUCKET_FILL")).orElse(XSound.ITEM_BUCKET_FILL))
                    .icon(XMaterial.matchXMaterial(bucketSection.getString("icon", "WATER_BUCKET")).orElse(XMaterial.WATER_BUCKET))
                    .allowAutomation(bucketSection.getBoolean("allow-automation", false))
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
        config.set(path + "allow-automation", template.isAllowAutomation());

        plugin.getConfigManager().saveBucketsConfig();
    }

    public void deleteTemplate(String id) {
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        config.set("buckets." + id, null);
        plugin.getConfigManager().saveBucketsConfig();
    }

    /**
     * Converts the pre-26.9 format ({@code presets:} / {@code customBuckets:}) into {@code buckets:} entries.
     * Runs even if {@code buckets:} already exists, since older builds merged the default buckets into
     * old-format files and that used to make this step skip itself.
     */
    public void migrate() {
        FileConfiguration config = plugin.getConfigManager().getBucketsConfig();
        if (!config.contains("presets") && !config.contains("customBuckets")) return;

        plugin.getLogger().info("Migrating old buckets.yml format...");
        backupBucketsFile();

        List<ConfigurationSection> oldBuckets = new ArrayList<>();
        ConfigurationSection presets = config.getConfigurationSection("presets");
        if (presets != null) {
            for (String key : presets.getKeys(false)) {
                ConfigurationSection preset = presets.getConfigurationSection(key);
                if (preset != null) oldBuckets.add(preset);
            }
        }
        List<?> customBuckets = config.getList("customBuckets");
        if (customBuckets != null) {
            for (Object entry : customBuckets) {
                // createSection turns nested maps (permissions, behavior) into sections so their paths resolve
                if (entry instanceof Map<?, ?> map) oldBuckets.add(new MemoryConfiguration().createSection("bucket", map));
            }
        }

        int migrated = 0;
        for (ConfigurationSection old : oldBuckets) {
            BucketTemplate template = fromOldFormat(old);
            if (template == null) continue;
            if (config.contains("buckets." + template.getId())) {
                plugin.getLogger().warning("Old bucket '" + template.getId() + "' not migrated: a bucket with that id already exists.");
                continue;
            }
            saveTemplate(template);
            migrated++;
        }

        config.set("presets", null);
        config.set("customBuckets", null);
        config.set("version", null);
        plugin.getConfigManager().saveBucketsConfig();
        plugin.getLogger().info("Migrated " + migrated + " bucket(s) from the old buckets.yml format.");
    }

    /** Maps one old-format bucket. Old items carry the {@code id} field, not the section key, so that is the template id. */
    private @Nullable BucketTemplate fromOldFormat(ConfigurationSection old) {
        String id = old.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning("Old bucket without an 'id' skipped during migration.");
            return null;
        }
        if (id.contains(".")) {
            plugin.getLogger().warning("Old bucket '" + id + "' skipped during migration: ids can't contain '.'.");
            return null;
        }
        if (!old.getBoolean("enabled", true)) return null;

        BucketTemplate.BucketMode mode;
        try {
            mode = BucketTemplate.BucketMode.valueOf(old.getString("mode", "vanilla_like").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Old bucket '" + id + "' skipped during migration: mode " + old.getString("mode") + " no longer exists.");
            return null;
        }

        List<String> fluids = mode == BucketTemplate.BucketMode.DRAIN_AREA
                ? old.getStringList("behavior.fluids")
                : old.getStringList("fluids");
        Material liquid = fluids.stream().map(BucketStorage::stripNamespace).map(Material::matchMaterial)
                .filter(m -> m != null && BucketTemplate.PLACEABLE_LIQUIDS.contains(m))
                .findFirst().orElse(Material.WATER);

        // Old builds required infb.use.<id> when no permission was set
        String permission = old.getString("permissions.use", "");
        if (permission.isBlank()) permission = "infb.use." + id;

        // Old builds always rendered lore without italics
        List<String> lore = old.getStringList("lore").stream()
                .map(line -> line.startsWith("<!i>") || line.startsWith("<!italic>") ? line : "<!i>" + line)
                .collect(Collectors.toList());

        String fallbackIcon = mode == BucketTemplate.BucketMode.DRAIN_AREA ? "BUCKET" : BucketTemplate.bucketIcon(liquid).name();
        return BucketTemplate.builder()
                .id(id)
                .displayName(old.getString("displayName", id))
                .lore(lore)
                .liquidType(liquid)
                .mode(mode)
                .usageLimit(old.getInt("uses", -1))
                .cooldown(0)
                .permission(permission)
                .glowing(true)
                .customModelData(old.getInt("customModelData", 0))
                .placeSound(XSound.ITEM_BUCKET_EMPTY)
                .refillSound(XSound.ITEM_BUCKET_FILL)
                .icon(XMaterial.matchXMaterial(stripNamespace(old.getString("icon", fallbackIcon))).orElse(XMaterial.WATER_BUCKET))
                .allowAutomation(false)
                .build();
    }

    private static String stripNamespace(String name) {
        int colon = name.indexOf(':');
        return (colon >= 0 ? name.substring(colon + 1) : name).toUpperCase();
    }

    private void backupBucketsFile() {
        File source = new File(plugin.getDataFolder(), "buckets.yml");
        if (!source.exists()) return;
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File target = new File(plugin.getDataFolder(), "backups/buckets.yml.pre-migration-" + stamp);
        try {
            target.getParentFile().mkdirs();
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not back up buckets.yml before migrating: " + e.getMessage());
        }
    }
}
