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

    public enum BucketMode {
        VANILLA_LIKE, DRAIN_AREA
    }
}
