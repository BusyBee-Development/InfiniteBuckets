package me.djtmk.InfiniteBuckets.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class VersionCheck implements Listener {

    private final Main plugin;
    private String latestVersion;

    public VersionCheck(Main plugin) {
        this.plugin = plugin;
        this.fetchLatestVersion();
    }

    private void fetchLatestVersion() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/infinitebuckets/version");
                InputStreamReader reader = new InputStreamReader(url.openStream());
                JsonArray versionList = JsonParser.parseReader(reader).getAsJsonArray();
                if (!versionList.isEmpty()) {
                    JsonObject latestVersionObject = versionList.get(0).getAsJsonObject();
                    this.latestVersion = latestVersionObject.get("version_number").getAsString();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for Modrinth updates.");
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (latestVersion == null || !player.hasPermission("infb.admin")) {
            return;
        }

        String currentVersion = plugin.getDescription().getVersion();
        if (isNewerVersion(latestVersion, currentVersion)) {
            List<String> lines = plugin.getMessageManager().getMessagesConfig().getStringList("update-notifier");
            MiniMessage mm = MiniMessage.miniMessage();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                Component component = mm.deserialize(line,
                        Placeholder.unparsed("current_version", currentVersion),
                        Placeholder.unparsed("new_version", latestVersion)
                );

                if (i == lines.size() - 1) {
                    component = component.clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/infinitebuckets"));
                }
                player.sendMessage(component);
            }
        }
    }

    private boolean isNewerVersion(String version1, String version2) {
        String v1 = version1.replaceAll("[^\\d.]", "");
        String v2 = version2.replaceAll("[^\\d.]", "");

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int num1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int num2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;
            if (num1 > num2) {
                return true;
            }
            if (num1 < num2) {
                return false;
            }
        }
        return false;
    }
}
