package net.busybee.InfiniteBuckets.hooks.protectionhook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ProtectionHook {

    /** What a bucket does to a block: placing liquid, or draining it like filling a bucket. */
    enum Action { PLACE, BREAK }

    boolean canBuild(Player player, Block block);

    /** Hooks that tell placing from breaking override this; the rest treat both as building. */
    default boolean canBuild(Player player, Block block, Action action) {
        return canBuild(player, block);
    }

    default boolean canBuild(Block block) {
        return true;
    }
}
