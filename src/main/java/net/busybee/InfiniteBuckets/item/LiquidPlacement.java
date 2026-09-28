package net.busybee.InfiniteBuckets.item;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;

/** Places a bucket's liquid the way a vanilla bucket would. Call it on the thread that owns {@code target}. */
public final class LiquidPlacement {

    private LiquidPlacement() {
    }

    public static boolean place(@NotNull Block target, @NotNull Material liquid) {
        if (target.getBlockData() instanceof Waterlogged waterlogged) {
            if (liquid != Material.WATER || waterlogged.isWaterlogged()) return false;
            waterlogged.setWaterlogged(true);
            target.setBlockData(waterlogged);
            return true;
        }

        Material type = target.getType();
        if (!type.isAir() && !target.isLiquid()) {
            // Grass, flowers, torches and the like are broken, solid blocks are left alone
            if (!target.isReplaceable() && type.isSolid()) return false;
            target.breakNaturally();
        }

        target.setType(liquid);
        return true;
    }
}
