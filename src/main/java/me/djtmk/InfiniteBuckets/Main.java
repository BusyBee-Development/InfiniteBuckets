package me.djtmk.InfiniteBuckets;

import me.djtmk.InfiniteBuckets.commands.Commands;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.item.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ItemManager itemManager;
    private boolean debugEnabled;
    private boolean isSuperiorSkyblockEnabled;

    @Override
    public void onEnable() {
        instance = this;

        // Check if SuperiorSkyblock2 is present
        isSuperiorSkyblockEnabled = hasIslandPlugin();
        if (!isSuperiorSkyblockEnabled) {
            getLogger().warning("SuperiorSkyblock2 is not installed on the server. Island checks will be disabled.");
        } else {
            getLogger().info("SuperiorSkyblock2 has been found on the server. Island checks enabled.");
        }

        saveDefaultConfig();
        debugEnabled = getConfig().getBoolean("debug", false);
        getLogger().info("Debug mode: " + (debugEnabled ? "enabled" : "disabled"));
        itemManager = new ItemManager(this);
        registerCommands();
        registerListeners();

        getLogger().info(String.format("[%s] v%s enabled successfully!", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        getLogger().info(String.format("[%s] v%s disabled!", getDescription().getName(), getDescription().getVersion()));
        instance = null;
    }

    private boolean hasIslandPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2");
        return plugin != null && plugin.isEnabled();
    }

    private void registerCommands() {
        if (this.getCommand("infb") == null) {
            getLogger().severe("Command 'infb' not found in plugin.yml! Plugin will not function properly!");
            setEnabled(false);
            return;
        }
        Commands commands = new Commands(this);
        this.getCommand("infb").setExecutor(commands);
        this.getCommand("infb").setTabCompleter(commands);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemEvents(this), this);
    }

    public static Main getInstance() {
        return instance;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        getConfig().set("debug", enabled);
        saveConfig();
        getLogger().info("Debug mode: " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isSuperiorSkyblockEnabled() {
        return isSuperiorSkyblockEnabled;
    }

    public void debugLog(String message, ItemStack item) {
        if (debugEnabled) {
            String itemInfo = item != null ? item.getType().toString() : "null";
            if (item != null && item.hasItemMeta()) {
                itemInfo += ", meta: " + item.getItemMeta().toString();
            }
            getLogger().info("[DEBUG] " + message + " [Item: " + itemInfo + "]");
        }
    }

    public void debugLog(String message) {
        if (debugEnabled) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}