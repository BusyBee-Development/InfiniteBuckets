package me.djtmk.InfiniteBuckets;

import me.djtmk.InfiniteBuckets.commands.Commands;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.item.ItemManager;
import me.djtmk.InfiniteBuckets.utils.ConfigKey;
import me.djtmk.InfiniteBuckets.utils.StringUtils;
import me.djtmk.InfiniteBuckets.utils.VersionCheck;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for InfiniteBuckets.
 * Handles plugin initialization, configuration, and core functionality.
 */
public class Main extends JavaPlugin implements Listener {

    // Constants
    private static final String DEBUG_CONFIG_KEY = "debug";
    private static final String LAST_NOTIFIED_VERSION_KEY = "lastNotifiedVersion";
    private static final String DEFAULT_VERSION = "0.0.0";
    private static final String DEBUG_PREFIX = "[DEBUG] ";

    // Plugin instance and components
    private static Main instance;
    private ItemManager itemManager;
    private ItemEvents itemEvents;
    private boolean debugEnabled;
    private boolean isSuperiorSkyblockEnabled;
    private final String currentVersion = getDescription().getVersion();
    private VersionCheck versionCheck;

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, loads configuration, and registers commands and event listeners.
     */
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
        debugEnabled = ConfigKey.DEBUG.getBoolean(this, false);
        getLogger().info("Debug mode: " + (debugEnabled ? "enabled" : "disabled"));
        itemManager = new ItemManager(this);
        versionCheck = new VersionCheck(this);
        registerCommands();
        registerListeners();
        updateAllConfigs();

        // Check for updates and log to console during startup
        versionCheck.checkForUpdates();

        getLogger().info(String.format("[%s] v%s enabled successfully!", getDescription().getName(), getDescription().getVersion()));
    }

    /**
     * Called when the plugin is disabled.
     * Performs cleanup operations and logs shutdown message.
     */
    @Override
    public void onDisable() {
        getLogger().info(String.format("[%s] v%s disabled!", getDescription().getName(), getDescription().getVersion()));
        instance = null;
    }

    /**
     * Checks if SuperiorSkyblock2 plugin is installed and enabled.
     *
     * @return true if SuperiorSkyblock2 is available, false otherwise
     */
    private boolean hasIslandPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2");
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Registers all plugin commands and their executors.
     * Disables the plugin if the main command is not found in plugin.yml.
     */
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

    /**
     * Registers all event listeners for the plugin.
     * This includes the ItemEvents listener and this class as a listener.
     */
    private void registerListeners() {
        itemEvents = new ItemEvents(this);
        getServer().getPluginManager().registerEvents(itemEvents, this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Updates all components with the latest configuration
     */
    public void updateAllConfigs() {
        if (itemEvents != null) {
            itemEvents.updateConfig();
        }
        // Add other components that need config updates here
    }

    /**
     * Handles player join events, checking for updates for OP players.
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            if (debugEnabled) debugLog("Player " + player.getName() + " is not an OP, skipping update notification.");
            return;
        }

        checkAndNotifyUpdate(player);
    }

    /**
     * Checks for updates and notifies the player if a new version is available.
     *
     * @param player The player to notify
     */
    private void checkAndNotifyUpdate(Player player) {
        String latestVersion = versionCheck.getLatestVersion();
        String lastNotifiedVersion = ConfigKey.LAST_NOTIFIED_VERSION.getString(this, DEFAULT_VERSION);

        if (debugEnabled) {
            debugLog("Checking update for OP " + player.getName() + ": Current=" + currentVersion +
                    ", Latest=" + latestVersion + ", LastNotified=" + lastNotifiedVersion +
                    ", isNewer=" + versionCheck.isNewerVersion(latestVersion, currentVersion) +
                    ", versionMismatch=" + !currentVersion.equals(latestVersion) +
                    ", notNotified=" + !latestVersion.equals(lastNotifiedVersion));
        }

        if (!currentVersion.equals(latestVersion) && versionCheck.isNewerVersion(latestVersion, currentVersion) && !latestVersion.equals(lastNotifiedVersion)) {
            TextComponent message = new TextComponent(StringUtils.format("&6-----------------------------------------\n"));
            TextComponent updateNotice = new TextComponent(StringUtils.format("&c[InfiniteBuckets] &aA new version is available! (v" + latestVersion + ")\n"));
            TextComponent currentVersionText = new TextComponent(StringUtils.format("&eYou're on: &c" + currentVersion + "\n"));
            TextComponent downloadText = new TextComponent(StringUtils.format("&aDownload the latest version from Modrinth: "));

            TextComponent clickHereText = new TextComponent(StringUtils.format("&b[Click Here]"));
            clickHereText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/infinitebuckets"));

            TextComponent newLine = new TextComponent(StringUtils.format("\n"));
            TextComponent endLine = new TextComponent(StringUtils.format("&6-----------------------------------------"));

            message.addExtra(updateNotice);
            message.addExtra(currentVersionText);
            message.addExtra(downloadText);
            message.addExtra(clickHereText);
            message.addExtra(newLine);
            message.addExtra(endLine);

            player.spigot().sendMessage(message);

            ConfigKey.LAST_NOTIFIED_VERSION.set(this, latestVersion);
            saveConfig();
        }
    }

    /**
     * Gets the singleton instance of the Main plugin class.
     *
     * @return The plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Gets the ItemManager instance that handles bucket creation.
     *
     * @return The ItemManager instance
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Sets the debug mode status and updates the configuration.
     *
     * @param enabled true to enable debug mode, false to disable
     */
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        ConfigKey.DEBUG.set(this, enabled);
        saveConfig();
        getLogger().info("Debug mode: " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Checks if SuperiorSkyblock2 integration is enabled.
     *
     * @return true if SuperiorSkyblock2 integration is enabled, false otherwise
     */
    public boolean isSuperiorSkyblockEnabled() {
        return isSuperiorSkyblockEnabled;
    }

    /**
     * Logs a debug message with item information if debug mode is enabled.
     *
     * @param message The debug message to log
     * @param item The ItemStack to include information about
     */
    public void debugLog(String message, ItemStack item) {
        if (debugEnabled) {
            String itemInfo = item != null ? item.getType().toString() : "null";
            if (item != null && item.hasItemMeta()) {
                itemInfo += ", meta: " + item.getItemMeta().toString();
            }
            getLogger().info(DEBUG_PREFIX + message + " [Item: " + itemInfo + "]");
        }
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message The debug message to log
     */
    public void debugLog(String message) {
        if (debugEnabled) {
            getLogger().info(DEBUG_PREFIX + message);
        }
    }
}
