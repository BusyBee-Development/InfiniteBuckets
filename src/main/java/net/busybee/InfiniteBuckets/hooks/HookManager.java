package net.busybee.InfiniteBuckets.hooks;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.core.ConfigManager;
import net.busybee.InfiniteBuckets.hooks.bentobox.BentoBoxHook;
import net.busybee.InfiniteBuckets.hooks.lands.LandsHook;
import net.busybee.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import net.busybee.InfiniteBuckets.hooks.superiorskyblock.SuperiorSkyblockHook;
import net.busybee.InfiniteBuckets.hooks.worldguard.v7.WorldGuard_v7;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class HookManager {

    private final List<ProtectionHook> activeHooks = new ArrayList<>();
    private final Map<String, Supplier<ProtectionHook>> availableHooks = new HashMap<>();

    public HookManager(Main plugin) {
        registerDefaultHooks();
        
        ConfigManager config = plugin.getConfigManager();
        if (!config.isAutoDetectHooks()) {
            plugin.getDebugLogger().debug("Auto-detect hooks is disabled.");
            return;
        }

        PluginManager pm = plugin.getServer().getPluginManager();

        for (Map.Entry<String, Supplier<ProtectionHook>> entry : availableHooks.entrySet()) {
            String pluginName = entry.getKey();
            
            if (!config.isHookEnabled(pluginName)) {
                plugin.getDebugLogger().debug("Hook for " + pluginName + " is disabled in config.");
                continue;
            }

            if (!pm.isPluginEnabled(pluginName)) {
                continue;
            }

            ProtectionHook hook = entry.getValue().get();
            if (hook != null) {
                activeHooks.add(hook);
                plugin.getLogger().info("Hooked into " + pluginName + ".");
            }
        }
    }

    private void registerDefaultHooks() {
        availableHooks.put("WorldGuard", () -> {
            Plugin wgPlugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin != null && wgPlugin.getDescription().getVersion().startsWith("7")) {
                return new WorldGuard_v7();
            }
            Main.getInstance().getLogger().warning("Unsupported WorldGuard version detected. The WorldGuard hook will not be enabled.");
            return null;
        });
        availableHooks.put("Lands", LandsHook::new);
        availableHooks.put("SuperiorSkyblock2", SuperiorSkyblockHook::new);
        availableHooks.put("BentoBox", BentoBoxHook::new);
    }

    public void registerHook(String pluginName, Supplier<ProtectionHook> hookSupplier) {
        availableHooks.put(pluginName, hookSupplier);
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
