package me.djtmk.InfiniteBuckets.hooks;

import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.hooks.lands.LandsHook;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import me.djtmk.InfiniteBuckets.hooks.superiorskyblock.SuperiorSkyblockHook;
import me.djtmk.InfiniteBuckets.hooks.worldguard.v7.WorldGuard_v7;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public final class HookManager {

    private final List<ProtectionHook> activeHooks = new ArrayList<>();

    public HookManager(Main plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();

        for (String pluginName : List.of("WorldGuard", "Lands", "SuperiorSkyblock2")) {

            if (!pm.isPluginEnabled(pluginName)) {
                continue;
            }

            ProtectionHook hook = null;
            if (pluginName.equals("WorldGuard")) {
                Plugin wgPlugin = pm.getPlugin("WorldGuard");
                if (wgPlugin != null && wgPlugin.getDescription().getVersion().startsWith("7")) {
                    hook = new WorldGuard_v7();
                } else {
                    plugin.getLogger().warning("Unsupported WorldGuard version detected. The WorldGuard hook will not be enabled.");
                }
            } else {
                hook = switch (pluginName) {
                    case "Lands" -> new LandsHook();
                    case "SuperiorSkyblock2" -> new SuperiorSkyblockHook();
                    default -> null;
                };
            }

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
