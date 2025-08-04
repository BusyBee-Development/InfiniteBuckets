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

        if (pm.isPluginEnabled("WorldGuard")) {
            activeHooks.add(new WorldGuardHook());
            plugin.getLogger().info("Hooked into WorldGuard.");
        }
        if (pm.isPluginEnabled("GriefPrevention")) {
            activeHooks.add(new GriefPreventionHook());
            plugin.getLogger().info("Hooked into GriefPrevention.");
        }
        if (pm.isPluginEnabled("Towny")) {
            activeHooks.add(new TownyHook());
            plugin.getLogger().info("Hooked into Towny.");
        }
        if (pm.isPluginEnabled("Lands")) {
            activeHooks.add(new LandsHook());
            plugin.getLogger().info("Hooked into Lands.");
        }
        if (pm.isPluginEnabled("PlotSquared")) {
            activeHooks.add(new PlotSquaredHook());
            plugin.getLogger().info("Hooked into PlotSquared.");
        }
        if (pm.isPluginEnabled("Residence")) {
            activeHooks.add(new ResidenceHook());
            plugin.getLogger().info("Hooked into Residence.");
        }
        if (pm.isPluginEnabled("SuperiorSkyblock2")) {
            activeHooks.add(new SuperiorSkyblockHook());
            plugin.getLogger().info("Hooked into SuperiorSkyblock2.");
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
