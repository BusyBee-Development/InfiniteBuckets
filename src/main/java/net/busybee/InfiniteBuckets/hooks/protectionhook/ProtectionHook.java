package net.busybee.InfiniteBuckets.hooks.protectionhook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ProtectionHook {
    boolean canBuild(Player player, Block block);

    /**
     * Player-less check used for automation contexts (e.g. dispensers) where
     * there is no player to evaluate permissions/membership against. Defaults
     * to fail-open, matching how hooks already behave for locations with no
     * region/island data.
     */
    default boolean canBuild(Block block) {
        return true;
    }
}
