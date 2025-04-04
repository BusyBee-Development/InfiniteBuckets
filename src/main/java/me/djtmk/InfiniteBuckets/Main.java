package me.djtmk.InfiniteBuckets;

import me.djtmk.InfiniteBuckets.commands.Commands;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.item.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        instance = this;
        itemManager = new ItemManager(this);
        saveDefaultConfig();
        registerCommands();
        registerListeners();

        getLogger().info(String.format("[%s] Plugin enabled successfully!", getDescription().getName()));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling InfiniteBuckets plugin... Cleaning up!");
    }

    private void registerCommands() {
        if (this.getCommand("inf") != null) {
            this.getCommand("inf").setExecutor(new Commands(this));
        } else {
            getLogger().warning("Command 'inf' not found in plugin.yml!");
        }
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
}