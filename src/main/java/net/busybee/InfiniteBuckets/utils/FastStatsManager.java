package net.busybee.InfiniteBuckets.utils;

import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import net.busybee.InfiniteBuckets.Main;

import java.lang.reflect.InvocationTargetException;

public class FastStatsManager {
    private static final String FASTSTATS_TOKEN = "8a7fc8335d030596b994c3ee77190a85";

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(InvocationTargetException.class);

    private final Main plugin;
    private final BukkitContext context;

    public FastStatsManager(Main plugin) {
        this.plugin = plugin;
        this.context = createContext(plugin);
    }

    // Metrics are optional: a missing or rejected token must never stop the plugin from enabling.
    private static BukkitContext createContext(Main plugin) {
        if (!FASTSTATS_TOKEN.matches("[a-z0-9]{32}")) {
            plugin.getLogger().warning("FastStats token is not set; metrics are disabled.");
            return null;
        }
        try {
            return new BukkitContext.Factory(plugin, FASTSTATS_TOKEN)
                    .errorTrackerService(ERROR_TRACKER)
                    .metrics(factory -> factory
                            .addMetric(Metric.number("registered_buckets", () -> plugin.getBucketRegistry().getRegisteredTemplates().size()))
                            .create())
                    .create();
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Could not start FastStats; metrics are disabled: " + e.getMessage());
            return null;
        }
    }

    public void onEnable() {
        if (context == null) return;
        context.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        if (context != null) context.shutdown();
    }
}
