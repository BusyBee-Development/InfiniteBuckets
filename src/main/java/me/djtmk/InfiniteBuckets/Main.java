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
import java.io.InputStreamReader;
import java.net.URL;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private ItemManager itemManager;
    private boolean debugEnabled;
    private boolean isSuperiorSkyblockEnabled;
    private final String currentVersion = getDescription().getVersion();

    @Override
    public void onEnable() {
        instance = this;

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

        // Check for updates asynchronously
        checkForUpdates();

        getLogger().info(String.format("[%s] v%s enabled successfully!", getDescription().getName(), currentVersion));
    }

    @Override
    public void onDisable() {
        getLogger().info(String.format("[%s] v%s disabled!", getDescription().getName(), currentVersion));
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
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/OneBlock-Odyssey/InfiniteBuckets/main/version.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                String lastNotifiedVersion = getConfig().getString("lastNotifiedVersion", "0.0.0");

                if (isNewerVersion(latestVersion, currentVersion)) {
                    getLogger().warning("-------------------------------------------");
                    getLogger().warning("A new version of InfiniteBuckets is available!");
                    getLogger().warning("Current: " + currentVersion + " | Latest: " + latestVersion);
                    getLogger().warning("Download the latest version here:");
                    getLogger().warning("BuiltByBit: https://builtbybit.com/resources/infinitebuckets.61863/");
                    getLogger().warning("Modrinth:   https://modrinth.com/plugin/infinitebuckets");
                    getLogger().warning("-------------------------------------------");

                    if (!latestVersion.equals(lastNotifiedVersion)) {
                        getConfig().set("lastNotifiedVersion", latestVersion);
                        saveConfig();
                    }
                } else {
                    getLogger().info("InfiniteBuckets is up to date (v" + currentVersion + ").");
                }

            } catch (Exception e) {
                getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) return;

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/OneBlock-Odyssey/InfiniteBuckets/main/version.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                String lastNotifiedVersion = getConfig().getString("lastNotifiedVersion", "0.0.0");

                if (isNewerVersion(latestVersion, currentVersion) && !latestVersion.equals(lastNotifiedVersion)) {
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

                    Bukkit.getScheduler().runTask(this, () -> player.spigot().sendMessage(message));

                    getConfig().set("lastNotifiedVersion", latestVersion);
                    saveConfig();
                }
            } catch (Exception e) {
                getLogger().warning("Could not check for updates on player join: " + e.getMessage());
            }
        });
    }

    private boolean isNewerVersion(String latest, String current) {
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
