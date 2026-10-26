package net.busybee.InfiniteBuckets.bucket;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketTemplate {
    /** Block types a bucket can place. Anything else can't be set as a block. */
    public static final List<Material> PLACEABLE_LIQUIDS = List.of(Material.WATER, Material.LAVA, Material.POWDER_SNOW);
    public static final int DEFAULT_DRAIN_RADIUS = 3;
    /** Keeps one click from scanning (and loading) a huge area. 16 is a 33x33x33 cube. */
    public static final int MAX_DRAIN_RADIUS = 16;

    private String id;
    private String displayName;
    private List<String> lore;
    private Material liquidType;
    private BucketMode mode;
    private int usageLimit;
    private long cooldown;
    private String permission;
    private boolean glowing;
    private int customModelData;
    private XSound placeSound;
    private XSound refillSound;
    private XMaterial icon;
    private boolean allowAutomation;
    /** DRAIN_AREA only: how far the drained cube reaches from the clicked block on each axis. */
    @Builder.Default
    private int drainRadius = DEFAULT_DRAIN_RADIUS;
    /** DRAIN_AREA only: place a real block (the icon) like a vanilla sponge and drain around it, using up the item. */
    private boolean placeBlock;

    public static Material bucketIcon(Material liquid) {
        return switch (liquid) {
            case LAVA -> Material.LAVA_BUCKET;
            case POWDER_SNOW -> Material.POWDER_SNOW_BUCKET;
            default -> Material.WATER_BUCKET;
        };
    }

    /** The icon a bucket gets unless someone picked their own: an empty bucket for drain buckets, the filled one otherwise. */
    public static XMaterial defaultIcon(BucketMode mode, Material liquid) {
        return mode == BucketMode.DRAIN_AREA ? XMaterial.BUCKET : XMaterial.matchXMaterial(bucketIcon(liquid));
    }

    /** Whether the icon is one of the built-in bucket icons rather than one picked by hand. */
    public boolean hasDefaultIcon() {
        return icon == XMaterial.BUCKET || icon == XMaterial.WATER_BUCKET || icon == XMaterial.LAVA_BUCKET
                || icon == XMaterial.POWDER_SNOW_BUCKET;
    }

    public enum BucketMode {
        VANILLA_LIKE, DRAIN_AREA
    }
}
