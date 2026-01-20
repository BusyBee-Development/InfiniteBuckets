package me.djtmk.InfiniteBuckets.hooks.bentobox;

import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.lists.Flags;

public final class BentoBoxHook implements ProtectionHook {

    private static Flag bucketFlag = null;
    private static boolean checkedForBucketFlag = false;

    @Override
    public boolean canBuild(Player player, Block block) {
        BentoBox bentoBox = BentoBox.getInstance();
        if (bentoBox == null || !bentoBox.isEnabled()) {
            return true;
        }

        if (!bentoBox.getIWM().inWorld(block.getLocation())) {
            return true;
        }

        Island island = bentoBox.getIslands().getIslandAt(block.getLocation()).orElse(null);
        if (island == null) {
            return true;
        }

        User user = User.getInstance(player);

        // Try to use BUCKET flag if available, otherwise fall back to PLACE_BLOCKS
        if (!checkedForBucketFlag) {
            try {
                bucketFlag = Flags.BUCKET;
                Bukkit.getLogger().info("[InfiniteBuckets] Using BentoBox BUCKET flag for permission checks");
            } catch (NoSuchFieldError e) {
                bucketFlag = null;
                Bukkit.getLogger().info("[InfiniteBuckets] BUCKET flag not available, using PLACE_BLOCKS for BentoBox permission checks");
            }
            checkedForBucketFlag = true;
        }

        Flag flagToCheck = (bucketFlag != null) ? bucketFlag : Flags.PLACE_BLOCKS;
        return island.isAllowed(user, flagToCheck);
    }
}
