package me.djtmk.InfiniteBuckets.item;

import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public record InfiniteBucket(String id, Material material, Component displayName,
                             List<Component> lore, String usePermission, String craftPermission,
                             boolean worksInNether, BucketMode mode, String action,
                             List<String> allowedFluids, List<String> allowedFluidTags,
                             int capacity, DrainBehavior drainBehavior, boolean craftingEnabled,
                             String craftingRecipe, Integer uses, Integer customModelData) {

    public enum BucketMode {
        VANILLA_LIKE,
        DRAIN_AREA,
        EFFECT
    }

    public record DrainBehavior(int radius, int maxBlocksPerUse, int cooldown,
                                boolean waterlogged, List<String> drainFluids,
                                List<String> drainFluidTags, boolean unsafe) {
    }

    public static final NamespacedKey BUCKET_ID_KEY = new NamespacedKey(Main.getInstance(), "infinite_bucket_id");
    public static final NamespacedKey BUCKET_USES_KEY = new NamespacedKey(Main.getInstance(), "infinite_bucket_uses");

    public ItemStack createItem(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(BUCKET_ID_KEY, PersistentDataType.STRING, this.id);
        meta.getPersistentDataContainer().set(BUCKET_USES_KEY, PersistentDataType.INTEGER, this.uses);

        meta.displayName(this.displayName);

        List<Component> updatedLore = updateLoreWithUses(this.lore, this.uses, this.uses); // Pass total uses
        if (!updatedLore.isEmpty()) {
            meta.lore(updatedLore);
        }

        if (this.customModelData != null) {
            meta.setCustomModelData(this.customModelData);
        }

        try {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        } catch (NoSuchFieldError e) {
            try {
                Enchantment unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
                if (unbreaking != null) {
                    meta.addEnchant(unbreaking, 1, true);
                }
            } catch (Exception ex) {
                Main.getInstance().getLogger().warning("Unable to add Unbreaking enchantment - incompatible Bukkit API version");
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    public String getUsePermission() {
        return usePermission != null && !usePermission.isBlank() ? usePermission : "infb.use." + id;
    }

    public String getCraftPermission() {
        return craftPermission != null && !craftPermission.isBlank() ? craftPermission : "infb.craft." + id;
    }

    public static Optional<InfiniteBucket> fromBucketsConfig(Main plugin, ConfigurationSection config) {
        MiniMessage mm = MiniMessage.miniMessage();

        String bucketId = config.getString("id");
        if (bucketId == null || bucketId.isBlank()) {
            plugin.getLogger().warning("Bucket configuration is missing required 'id' field. Skipping.");
            return Optional.empty();
        }

        String displayName = config.getString("displayName", "Invalid Bucket");
        Component displayNameComponent = mm.deserialize(displayName);

        List<Component> lore = config.getStringList("lore").stream()
                .map(mm::deserialize)
                .map(component -> component.decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());

        String modeString = config.getString("mode", "vanilla_like").toUpperCase();
        BucketMode mode;
        try {
            mode = BucketMode.valueOf(modeString);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid bucket mode '" + modeString + "' for bucket '" + bucketId + "'. Defaulting to VANILLA_LIKE.");
            mode = BucketMode.VANILLA_LIKE;
        }

        String icon = config.getString("icon");
        if (icon == null && mode == BucketMode.EFFECT) {
            icon = "minecraft:milk_bucket";
        } else if (icon == null) {
            icon = "minecraft:bucket";
        }

        Material material = deriveMaterial(icon, mode);

        ConfigurationSection permissionsSection = config.getConfigurationSection("permissions");
        String usePermission = permissionsSection != null ? permissionsSection.getString("use", "") : "";
        String craftPermission = permissionsSection != null ? permissionsSection.getString("craft", "") : "";
        String action = config.getString("action", "");

        List<String> allowedFluids = config.getStringList("fluids");
        List<String> allowedFluidTags = config.getStringList("fluidTags");
        int capacity = config.getInt("capacity", 1);
        Integer uses = config.contains("uses") ? config.getInt("uses") : -1;
        Integer customModelData = config.contains("customModelData") ? config.getInt("customModelData") : null;

        DrainBehavior drainBehavior = null;
        if (mode == BucketMode.DRAIN_AREA) {
            ConfigurationSection behaviorSection = config.getConfigurationSection("behavior");
            if (behaviorSection == null) {
                plugin.getLogger().warning("Bucket '" + bucketId + "' with mode 'drain_area' is missing required 'behavior' section. Skipping.");
                return Optional.empty();
            }

            int radius = behaviorSection.getInt("radius", 2);
            int maxBlocks = behaviorSection.getInt("maxBlocksPerUse", 200);
            int cooldown = behaviorSection.getInt("cooldown", 10);
            boolean waterlogged = behaviorSection.getBoolean("waterlogged", true);
            List<String> drainFluids = behaviorSection.getStringList("fluids");
            List<String> drainFluidTags = behaviorSection.getStringList("fluidTags");
            boolean unsafe = behaviorSection.getBoolean("unsafe", false);
            if (!unsafe) {
                radius = Math.min(radius, 6);
                maxBlocks = Math.min(maxBlocks, 5000);
            }

            drainBehavior = new DrainBehavior(radius, maxBlocks, cooldown, waterlogged, drainFluids, drainFluidTags, unsafe);
        }

        ConfigurationSection craftingSection = config.getConfigurationSection("crafting");
        boolean craftingEnabled = craftingSection != null && craftingSection.getBoolean("enabled", false);
        String craftingRecipe = craftingSection != null ? craftingSection.getString("recipe", "") : "";

        if (mode == BucketMode.VANILLA_LIKE && allowedFluids.isEmpty() && allowedFluidTags.isEmpty()) {
            plugin.getLogger().warning("Bucket '" + bucketId + "' with mode 'vanilla_like' must specify either 'fluids' or 'fluidTags'. Skipping.");
            return Optional.empty();
        }

        if (mode == BucketMode.DRAIN_AREA && drainBehavior.drainFluids().isEmpty() && drainBehavior.drainFluidTags().isEmpty()) {
            plugin.getLogger().warning("Bucket '" + bucketId + "' with mode 'drain_area' must specify 'fluids' or 'fluidTags' in its 'behavior' section. Skipping.");
            return Optional.empty();
        }

        if (mode == BucketMode.EFFECT && action.isBlank()) {
            plugin.getLogger().warning("Bucket '" + bucketId + "' with mode 'EFFECT' must specify an 'action'. Skipping.");
            return Optional.empty();
        }

        return Optional.of(new InfiniteBucket(
                bucketId, material, displayNameComponent, lore, usePermission, craftPermission, true,
                mode, action, allowedFluids, allowedFluidTags, capacity, drainBehavior, craftingEnabled, craftingRecipe, uses, customModelData
        ));
    }

    private static Material deriveMaterial(String icon, BucketMode mode) {
        Material material = Material.matchMaterial(icon);
        if (material != null && (material.name().endsWith("_BUCKET") || material == Material.BUCKET)) {
            return material;
        }

        if ("builtin:empty_bucket".equals(icon)) return Material.BUCKET;

        return switch (mode) {
            case VANILLA_LIKE -> Material.WATER_BUCKET;
            case DRAIN_AREA -> Material.BUCKET;
            case EFFECT -> Material.MILK_BUCKET;
        };
    }

    public static List<Component> updateLoreWithUses(List<Component> originalLore, int currentUses, int totalUses) {
        MiniMessage mm = MiniMessage.miniMessage();
        return originalLore.stream().map(component -> {
            String componentString = mm.serialize(component);
            if (componentString.contains("<uses>")) {
                if (totalUses == -1) {
                    return null;
                } else {
                    TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                            .matchLiteral("<uses>")
                            .replacement(currentUses + "/" + totalUses)
                            .build();
                    return component.replaceText(replacementConfig);
                }
            }
            return component;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Deprecated
    public static Optional<InfiniteBucket> fromConfig(Main plugin, String id, ConfigurationSection config) {return Optional.empty();}
}
