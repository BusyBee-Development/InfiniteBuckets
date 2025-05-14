package me.djtmk.InfiniteBuckets;

import me.djtmk.InfiniteBuckets.commands.Commands;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.item.ItemManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private ItemManager itemManager;
    private boolean debugEnabled;
    private boolean isSuperiorSkyblockEnabled;
    private final String currentVersion = getDescription().getVersion(); // Current plugin version
    private String latestVersionCache = null; // Cache for the latest version

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

        // Check for updates and log to console during startup
        checkForUpdates();

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
        getServer().getPluginManager().registerEvents(this, this); // Register this class as a listener
    }

    private void checkForUpdates() {
        latestVersionCache = fetchLatestVersion(); // Cache the latest version
        String lastNotifiedVersion = getConfig().getString("lastNotifiedVersion", "0.0.0");

        if (!currentVersion.equals(latestVersionCache) && isNewerVersion(latestVersionCache, currentVersion)) {
            getLogger().warning("-------------------------------------------");
            getLogger().warning("A new version of InfiniteBuckets is available!");
            getLogger().warning("Current: " + currentVersion + " | Latest: " + latestVersionCache);
            getLogger().warning("Download the latest version to stay up to date!");
            getLogger().warning("Links: https://builtbybit.com/resources/infinitebuckets.61863/");
            getLogger().warning("       https://modrinth.com/plugin/infinitebuckets");
            getLogger().warning("-------------------------------------------");
        } else if (debugEnabled) {
            debugLog("No new update available. Current: " + currentVersion + ", Latest: " + latestVersionCache + ", Last Notified: " + lastNotifiedVersion);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            if (debugEnabled) debugLog("Player " + player.getName() + " is not an OP, skipping update notification.");
            return;
        }

        String latestVersion = latestVersionCache != null ? latestVersionCache : fetchLatestVersion();
        String lastNotifiedVersion = getConfig().getString("lastNotifiedVersion", "0.0.0");

        if (debugEnabled) {
            debugLog("Checking update for OP " + player.getName() + ": Current=" + currentVersion +
                    ", Latest=" + latestVersion + ", LastNotified=" + lastNotifiedVersion +
                    ", isNewer=" + isNewerVersion(latestVersion, currentVersion) +
                    ", versionMismatch=" + !currentVersion.equals(latestVersion) +
                    ", notNotified=" + !latestVersion.equals(lastNotifiedVersion));
        }

        if (!currentVersion.equals(latestVersion) && isNewerVersion(latestVersion, currentVersion) && !latestVersion.equals(lastNotifiedVersion)) {
            // Prepare the message with clickable links for the OP player
            TextComponent message = new TextComponent(ChatColor.GOLD + "-----------------------------------------\n");
            TextComponent updateNotice = new TextComponent(ChatColor.RED + "[InfiniteBuckets] " + ChatColor.GREEN + "A new version is available! (v" + latestVersion + ")\n");
            TextComponent currentVersionText = new TextComponent(ChatColor.YELLOW + "You're on: " + ChatColor.RED + currentVersion + "\n");
            TextComponent downloadText = new TextComponent(ChatColor.GREEN + "Download the latest version here:\n");

            TextComponent builtByBitText = new TextComponent(ChatColor.YELLOW + "[BuiltByBit] ");
            TextComponent builtByBitLink = new TextComponent(ChatColor.WHITE + "https://builtbybit.com/resources/infinitebuckets.61863/\n");
            builtByBitLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://builtbybit.com/resources/infinitebuckets.61863/"));

            TextComponent modrinthText = new TextComponent(ChatColor.AQUA + "[Modrinth] ");
            TextComponent modrinthLink = new TextComponent(ChatColor.WHITE + "https://modrinth.com/plugin/infinitebuckets\n");
            modrinthLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/infinitebuckets"));

            TextComponent endLine = new TextComponent(ChatColor.GOLD + "-----------------------------------------");

            message.addExtra(updateNotice);
            message.addExtra(currentVersionText);
            message.addExtra(downloadText);
            message.addExtra(builtByBitText);
            message.addExtra(builtByBitLink);
            message.addExtra(modrinthText);
            message.addExtra(modrinthLink);
            message.addExtra(endLine);

            player.spigot().sendMessage(message);

            // Update the last notified version in config
            getConfig().set("lastNotifiedVersion", latestVersion);
            saveConfig();
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        // Simple version comparison (e.g., "1.4.5" > "1.4.4")
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int latestNum = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int currentNum = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (latestNum > currentNum) return true;
            if (latestNum < currentNum) return false;
        }
        return false;
    }

    private String fetchLatestVersion() {
        if (latestVersionCache != null) {
            return latestVersionCache; // Return cached version
        }
        try {
            URL url = new URL("https://raw.githubusercontent.com/OneBlock-Odyssey/InfiniteBuckets/main/src/main/version.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String version = in.readLine();
            in.close();
            if (version != null && version.matches("\\d+\\.\\d+\\.\\d+")) { // Validate version format
                latestVersionCache = version.trim();
                return latestVersionCache;
            } else {
                getLogger().warning("Invalid version format fetched from GitHub: " + version);
                return currentVersion;
            }
        } catch (IOException e) {
            getLogger().warning("Error fetching the latest version from GitHub: " + e.getMessage());
            if (debugEnabled) {
                debugLog("Failed URL: https://raw.githubusercontent.com/OneBlock-Odyssey/InfiniteBuckets/main/src/main/version.txt");
            }
            return currentVersion;
        }
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
