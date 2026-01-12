package me.djtmk.InfiniteBuckets.hooks.bentobox;

import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

public final class BentoBoxHook implements ProtectionHook {

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
        return island.isAllowed(user, Flags.PLACE_BLOCKS);
    }
}
