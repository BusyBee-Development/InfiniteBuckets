package me.djtmk.InfiniteBuckets.hooks;

import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.hooks.griefprevention.GriefPreventionHook;
import me.djtmk.InfiniteBuckets.hooks.lands.LandsHook;
import me.djtmk.InfiniteBuckets.hooks.plotsquared.PlotSquaredHook;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import me.djtmk.InfiniteBuckets.hooks.residence.ResidenceHook;
import me.djtmk.InfiniteBuckets.hooks.superiorskyblock.SuperiorSkyblockHook;
import me.djtmk.InfiniteBuckets.hooks.towny.TownyHook;
import me.djtmk.InfiniteBuckets.hooks.worldguard.WorldGuardHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public final class HookManager {

    private final List<ProtectionHook> activeHooks = new ArrayList<>();

    public HookManager(Main plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();

        for (String pluginName : List.of("WorldGuard", "GriefPrevention", "Towny", "Lands",
                "PlotSquared", "Residence", "SuperiorSkyblock2")) {

            if (!pm.isPluginEnabled(pluginName)) {
                continue;
            }

            ProtectionHook hook = switch (pluginName) {
                case "WorldGuard" -> new WorldGuardHook();
                case "GriefPrevention" -> new GriefPreventionHook();
                case "Towny" -> new TownyHook();
                case "Lands" -> new LandsHook();
                case "PlotSquared" -> new PlotSquaredHook();
                case "Residence" -> new ResidenceHook();
                case "SuperiorSkyblock2" -> new SuperiorSkyblockHook();
                default -> null;
            };

            if (hook != null) {
                activeHooks.add(hook);
                plugin.getLogger().info("Hooked into " + pluginName + ".");
            }
        }
    }

    public boolean canBuild(Player player, Block block) {
        for (ProtectionHook hook : activeHooks) {
            if (!hook.canBuild(player, block)) {
                return false;
            }
        }

        return true;
    }
}
