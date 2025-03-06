package me.djtmk.InfiniteBuckets;

import me.djtmk.InfiniteBuckets.commands.Commands;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.item.ItemManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance; // Singleton instance of the plugin
    private static Economy econ = null; // Vault economy provider

    private ItemManager itemManager;

    @Override
    public void onEnable() {
        // Set the plugin singleton instance
        instance = this;

        // Initialize the ItemManager for managing plugin items
        itemManager = new ItemManager(this);

        // Save the default configuration file if it doesn't already exist
        saveDefaultConfig();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Setup Vault economy integration
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to missing Vault dependency!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Validate and handle configurations (Optional, adjust as needed)
        validateConfig();

        // Log a success message upon initialization
        getLogger().info(String.format("[%s] Plugin enabled successfully!", getDescription().getName()));
    }

    @Override
    public void onDisable() {
        // Perform clean-up tasks when the plugin is disabled
        getLogger().info("Disabling InfiniteBuckets plugin... Cleaning up!");

        // Add additional logic here if you need to save runtime data or unregister services
    }

    /**
     * Registers the commands used by the plugin.
     */
    private void registerCommands() {
        if (this.getCommand("infwater") != null) {
            this.getCommand("infwater").setExecutor(new Commands(this));
        } else {
            getLogger().warning("Command 'infwater' not found in plugin.yml!");
        }

        if (this.getCommand("inflava") != null) {
            this.getCommand("inflava").setExecutor(new Commands(this));
        } else {
            getLogger().warning("Command 'inflava' not found in plugin.yml!");
        }

        if (this.getCommand("infinitebuckets") != null) {
            this.getCommand("infinitebuckets").setExecutor(new Commands(this));
        } else {
            getLogger().warning("Command 'infinitebuckets' not found in plugin.yml!");
        }
    }

    /**
     * Registers the event listeners for the plugin.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemEvents(this), this);
    }

    /**
     * Sets up the economy provider using Vault.
     *
     * @return true if the economy provider was successfully set up, false otherwise.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin not found. Economy features will not be enabled.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found. Economy features will not be available.");
            return false;
        }

        econ = rsp.getProvider();
        getLogger().info("Economy provider found: " + econ.getName());
        return true;
    }

    /**
     * Validates the configuration file for required settings and keys.
     */
    private void validateConfig() {
        if (!getConfig().contains("some.key")) {
            getLogger().warning("Missing 'some.key' in config.yml. Using default values.");
            // Optional: Add logic to set default values here if necessary
        }
    }

    /**
     * Gets the singleton instance of the plugin.
     *
     * @return the current instance of the plugin.
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Gets the economy provider.
     *
     * @return the current Economy instance.
     */
    public static Economy getEconomy() {
        return econ;
    }

    /**
     * Gets the ItemManager instance.
     *
     * @return the ItemManager instance.
     */
    public ItemManager getItemManager() {
        return itemManager;
    }
}