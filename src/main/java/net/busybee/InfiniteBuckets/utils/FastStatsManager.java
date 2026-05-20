package net.busybee.InfiniteBuckets.utils;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;
import net.busybee.InfiniteBuckets.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FastStatsManager {
    private final Main plugin;
    private final BukkitMetrics metrics;

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(java.lang.reflect.InvocationTargetException.class);

    public FastStatsManager(Main plugin) {
        this.plugin = plugin;
        String token = loadToken();

        this.metrics = BukkitMetrics.factory()
                .token(token)
                .errorTracker(ERROR_TRACKER)
                .addMetric(Metric.number("registered_buckets", () -> plugin.getBucketRegistry().getRegisteredTemplates().size()))
                .create(plugin);
    }

    private String loadToken() {
        Properties props = new Properties();
        try (InputStream is = plugin.getResource("faststats.properties")) {
            if (is != null) {
                props.load(is);
                return props.getProperty("token", "YOUR_TOKEN_HERE");
            }
        } catch (IOException ignored) {}
        return "YOUR_TOKEN_HERE";
    }

    public void onEnable() {
        metrics.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        metrics.shutdown();
    }
}
