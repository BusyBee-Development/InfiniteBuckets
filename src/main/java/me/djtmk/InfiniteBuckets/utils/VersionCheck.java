package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=107565").openStream()))) {
                this.latestVersion = reader.readLine();
            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates.");
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("infb.admin")) {
            return;
        }

        String currentVersion = plugin.getDescription().getVersion();
        if (latestVersion != null && !currentVersion.equalsIgnoreCase(latestVersion)) {
            String messageString = "<#00aaff>[InfiniteBuckets] <gray>A new version is available: <gold>" + latestVersion + "</gold>. Click to download.";
            Component message = MiniMessage.miniMessage().deserialize(messageString)
                    .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/infinitebuckets.107565/"));
            player.sendMessage(message);
        }
    }
}
