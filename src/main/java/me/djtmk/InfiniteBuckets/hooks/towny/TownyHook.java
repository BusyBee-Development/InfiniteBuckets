package me.djtmk.InfiniteBuckets.hooks.towny;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class TownyHook implements ProtectionHook {

    @Override
    public boolean canBuild(Player player, Block block) {
        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.BUILD);
    }
}
