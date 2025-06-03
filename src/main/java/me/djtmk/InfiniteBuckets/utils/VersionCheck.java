package me.djtmk.InfiniteBuckets.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import me.djtmk.InfiniteBuckets.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for checking plugin version updates from Modrinth.
 * Provides methods to fetch the latest version and compare it with the current version.
 */
public class VersionCheck {

    /**
     * The URL for the Modrinth API to fetch version information
     */
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/infinitebuckets/version";

    /**
     * Pattern for validating version numbers in the format x.y.z
     */
    private static final String VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";

    private final Main plugin;
    private final Logger logger;
    private final String currentVersion;
    private String latestVersionCache;

    /**
     * Constructs a new VersionCheck instance.
     *
     * @param plugin The main plugin instance
     */
    public VersionCheck(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentVersion = plugin.getDescription().getVersion();
        this.latestVersionCache = null;
    }

    /**
     * Gets the latest version of the plugin from Modrinth.
     * Uses a cached value if available to reduce API calls.
     *
     * @return The latest version string
     */
    public String getLatestVersion() {
        if (latestVersionCache != null) {
            return latestVersionCache;
        }
        return fetchLatestVersion();
    }

    /**
     * Compares two version strings to determine if the latest version is newer.
     * Handles semantic versioning in the format x.y.z.
     *
     * @param latest The latest version string
     * @param current The current version string
     * @return true if the latest version is newer than the current version
     */
    public boolean isNewerVersion(String latest, String current) {
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

    /**
     * Checks for updates and logs a message to the console if a new version is available.
     * This method is typically called during plugin startup.
     */
    public void checkForUpdates() {
        String latestVersion = getLatestVersion();
        String lastNotifiedVersion = ConfigKey.LAST_NOTIFIED_VERSION.getString(plugin, "0.0.0");

        if (!currentVersion.equals(latestVersion) && isNewerVersion(latestVersion, currentVersion)) {
            logger.warning("-------------------------------------------");
            logger.warning("A new version of InfiniteBuckets is available!");
            logger.warning("Current: " + currentVersion + " | Latest: " + latestVersion);
            logger.warning("Download the latest version to stay up to date!");
            logger.warning("Link: https://modrinth.com/plugin/infinitebuckets (Click Here)");
            logger.warning("-------------------------------------------");
        } else if (plugin.isDebugEnabled()) {
            plugin.debugLog("No new update available. Current: " + currentVersion + ", Latest: " + latestVersion + ", Last Notified: " + lastNotifiedVersion);
        }
    }

    /**
     * Fetches the latest version from the Modrinth API.
     * Handles network errors and invalid responses gracefully.
     *
     * @return The latest version string, or the current version if an error occurs
     */
    private String fetchLatestVersion() {
        try {
            URL url = new URL(MODRINTH_API_URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parse JSON response
            String json = response.toString();
            if (json.isEmpty() || json.equals("[]")) {
                logger.warning("Empty or no versions found in Modrinth API response");
                return currentVersion;
            }

            JsonArray versions = JsonParser.parseString(json).getAsJsonArray();
            if (versions.size() == 0) {
                logger.warning("No versions available in Modrinth API response");
                return currentVersion;
            }

            String version = versions.get(0).getAsJsonObject().get("version_number").getAsString();
            if (version.matches(VERSION_PATTERN)) {
                latestVersionCache = version.trim();
                return latestVersionCache;
            } else {
                logger.warning("Invalid version format fetched from Modrinth: " + version);
                return currentVersion;
            }
        } catch (IOException e) {
            logger.warning("Error fetching the latest version from Modrinth: " + e.getMessage());
            if (plugin.isDebugEnabled()) {
                plugin.debugLog("Failed URL: " + MODRINTH_API_URL);
            }
            return currentVersion;
        } catch (Exception e) {
            logger.warning("Error parsing Modrinth API response: " + e.getMessage());
            return currentVersion;
        }
    }
}
