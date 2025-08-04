package me.djtmk.InfiniteBuckets.hooks.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class SuperiorSkyblockHook implements ProtectionHook {
    @Override
    public boolean canBuild(Player player, Block block) {
        Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        if (island == null) {
            return true;
        }
        return island.hasPermission(player, IslandPrivilege.getByName("BUILD"));
    }
}
