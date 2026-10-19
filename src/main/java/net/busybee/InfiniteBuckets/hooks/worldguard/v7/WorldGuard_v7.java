package net.busybee.InfiniteBuckets.hooks.worldguard.v7;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.busybee.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/** Also covers plugins whose claims are WorldGuard regions, such as LandClaim. */
public final class WorldGuard_v7 implements ProtectionHook {

    @Override
    public boolean canBuild(Player player, Block block) {
        return canBuild(player, block, Action.PLACE);
    }

    /**
     * The same check WorldGuard makes for a vanilla bucket: build plus block-place when emptying,
     * build plus block-break when filling, and region bypass lets staff through.
     */
    @Override
    public boolean canBuild(Player player, Block block, Action action) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        if (platform.getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(block.getWorld()))) return true;

        StateFlag flag = action == Action.BREAK ? Flags.BLOCK_BREAK : Flags.BLOCK_PLACE;
        RegionQuery query = platform.getRegionContainer().createQuery();
        return query.testBuild(BukkitAdapter.adapt(block.getLocation()), localPlayer, flag);
    }

    @Override
    public boolean canBuild(Block block) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(block.getLocation()), (LocalPlayer) null, Flags.BUILD);
    }
}
