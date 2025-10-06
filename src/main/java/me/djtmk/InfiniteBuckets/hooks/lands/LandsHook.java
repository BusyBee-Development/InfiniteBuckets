package me.djtmk.InfiniteBuckets.hooks.lands;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.types.RoleFlag;
import me.angeschossen.lands.api.land.Area;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class LandsHook implements ProtectionHook {

    @Override
    public boolean canBuild(Player player, Block block) {
        Main plugin = Main.getInstance();
        LandsIntegration api = LandsIntegration.of(plugin);
        Area area = api.getArea(block.getLocation());
        if (area == null) {
            return true;
        }

        Object flagObj = api.getFlagRegistry().get("BLOCK_PLACE");
        if (flagObj == null) {
            plugin.getLogger().warning("Could not find the BLOCK_PLACE flag in the Lands API. Defaulting to allow.");
            return true;
        }

        if (!(flagObj instanceof RoleFlag roleFlag)) {
            plugin.getLogger().warning("BLOCK_PLACE flag is not a RoleFlag. Defaulting to allow.");
            return true;
        }

        RoleFlag blockPlaceFlag = roleFlag;
        return area.hasFlag(player.getUniqueId(), blockPlaceFlag);
    }
}
