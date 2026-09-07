package net.busybee.InfiniteBuckets.utils;

import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import net.busybee.InfiniteBuckets.Main;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class FastStatsManager {
    private final Main plugin;
    private final BukkitContext context;

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(InvocationTargetException.class);

    public FastStatsManager(Main plugin) {
        this.plugin = plugin;
        String token = loadToken();

        this.context = new BukkitContext.Factory(plugin, token)
                .errorTrackerService(ERROR_TRACKER)
                .metrics(factory -> factory
                        .addMetric(Metric.number("registered_buckets", () -> plugin.getBucketRegistry().getRegisteredTemplates().size()))
                        .create())
                .create();
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
        context.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        context.shutdown();
    }
}
