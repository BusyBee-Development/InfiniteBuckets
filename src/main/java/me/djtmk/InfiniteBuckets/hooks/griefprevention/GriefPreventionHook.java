package me.djtmk.InfiniteBuckets.hooks.griefprevention;

import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class GriefPreventionHook implements ProtectionHook {

    @Override
    public boolean canBuild(Player player, Block block) {
        return GriefPrevention.instance.allowBuild(player, block.getLocation()) == null;
    }
}
