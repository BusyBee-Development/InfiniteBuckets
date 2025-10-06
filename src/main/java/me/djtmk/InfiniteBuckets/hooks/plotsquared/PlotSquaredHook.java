package me.djtmk.InfiniteBuckets.hooks.plotsquared;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class PlotSquaredHook implements ProtectionHook {

    @Override
    public boolean canBuild(Player player, Block block) {
        PlotAPI api = new PlotAPI();
        Location plotLocation = Location.at(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        PlotPlayer<?> plotPlayer = api.wrapPlayer(player.getUniqueId());

        if (plotLocation.getPlot() == null) {
            return true;
        }

        return plotLocation.getPlot().isOwner(plotPlayer.getUUID()) || 
               plotLocation.getPlot().getTrusted().contains(plotPlayer.getUUID());
    }
}
