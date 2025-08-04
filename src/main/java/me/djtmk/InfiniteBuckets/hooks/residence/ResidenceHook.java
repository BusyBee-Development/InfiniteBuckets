package me.djtmk.InfiniteBuckets.hooks.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ResidenceHook implements ProtectionHook {
    @Override
    public boolean canBuild(Player player, Block block) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());
        if (residence == null) {
            return true;
        }
        return residence.getPermissions().playerHas(player, "build", true);
    }
}
