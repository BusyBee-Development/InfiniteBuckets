package me.djtmk.InfiniteBuckets.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import me.djtmk.InfiniteBuckets.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

public class VersionCheck {

    private final Main plugin;
    private final Logger logger;
    private final String currentVersion;
    private String latestVersionCache;

    public VersionCheck(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentVersion = plugin.getDescription().getVersion();
        this.latestVersionCache = null;
    }

    public String getLatestVersion() {
        if (latestVersionCache != null) {
            return latestVersionCache;
        }
        return fetchLatestVersion();
    }

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

    public void checkForUpdates() {
        String latestVersion = getLatestVersion();
        String lastNotifiedVersion = plugin.getConfig().getString("lastNotifiedVersion", "0.0.0");

        if (!currentVersion.equals(latestVersion) && isNewerVersion(latestVersion, currentVersion)) {
            logger.warning("-------------------------------------------");
            logger.warning("A new version of InfiniteBuckets is available!");
            logger.warning("Current: " + currentVersion + " | Latest: " + latestVersion);
            logger.warning("Download the latest version to stay up to date!");
            logger.warning("Links: https://builtbybit.com/resources/infinitebuckets.61863/");
            logger.warning("       https://modrinth.com/plugin/infinitebuckets");
            logger.warning("-------------------------------------------");
        } else if (plugin.isDebugEnabled()) {
            plugin.debugLog("No new update available. Current: " + currentVersion + ", Latest: " + latestVersion + ", Last Notified: " + lastNotifiedVersion);
        }
    }

    private String fetchLatestVersion() {
        try {
            URL url = new URL("https://api.modrinth.com/v2/project/infinitebuckets/version");
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
            if (version.matches("\\d+\\.\\d+\\.\\d+")) {
                latestVersionCache = version.trim();
                return latestVersionCache;
            } else {
                logger.warning("Invalid version format fetched from Modrinth: " + version);
                return currentVersion;
            }
        } catch (IOException e) {
            logger.warning("Error fetching the latest version from Modrinth: " + e.getMessage());
            if (plugin.isDebugEnabled()) {
                plugin.debugLog("Failed URL: https://api.modrinth.com/v2/project/infinitebuckets/version");
            }
            return currentVersion;
        } catch (Exception e) {
            logger.warning("Error parsing Modrinth API response: " + e.getMessage());
            return currentVersion;
        }
    }
}