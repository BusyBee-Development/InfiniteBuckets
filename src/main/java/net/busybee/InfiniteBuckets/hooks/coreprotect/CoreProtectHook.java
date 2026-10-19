package net.busybee.InfiniteBuckets.hooks.coreprotect;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Logs bucket changes to CoreProtect the way it logs a vanilla bucket, so lookups and rollbacks
 * cover them. Call it on the thread that owns the block.
 */
public final class CoreProtectHook {

    /** logPlacement/logRemoval with BlockData arrived in API 6. */
    private static final int MIN_API_VERSION = 6;

    private final CoreProtectAPI api;

    private CoreProtectHook(CoreProtectAPI api) {
        this.api = api;
    }

    /** The hook, or null when CoreProtect isn't running or is too old. */
    public static @Nullable CoreProtectHook create() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (!(plugin instanceof CoreProtect coreProtect)) return null;
        CoreProtectAPI api = coreProtect.getAPI();
        if (api == null || !api.isEnabled() || api.APIVersion() < MIN_API_VERSION) return null;
        return new CoreProtectHook(api);
    }

    /**
     * Call after the liquid is in. {@code replacedType}/{@code replacedData} are what was there before:
     * a plant the liquid washed away is logged as broken, so a rollback puts it back.
     */
    public void logLiquidPlaced(String user, Block block, Material replacedType, BlockData replacedData, Material liquid) {
        boolean waterlogged = replacedData instanceof Waterlogged;
        if (!waterlogged && !replacedType.isAir() && replacedType != Material.WATER && replacedType != Material.LAVA) {
            api.logRemoval(user, block.getLocation(), replacedType, replacedData);
        }
        // No block data, like CoreProtect's own bucket logging: a rollback then drains a waterlogged block instead of removing it
        api.logPlacement(user, block.getLocation(), liquid, null);
    }

    /** Call before the liquid is removed. */
    public void logLiquidRemoved(String user, Block block) {
        api.logRemoval(user, block.getLocation(), block.getType(), block.getBlockData());
    }
}
