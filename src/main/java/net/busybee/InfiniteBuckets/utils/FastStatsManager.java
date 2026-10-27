package net.busybee.InfiniteBuckets.utils;

import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import net.busybee.InfiniteBuckets.Main;

import java.lang.reflect.InvocationTargetException;

public class FastStatsManager {
    private static final String FASTSTATS_TOKEN = "PASTE_FASTSTATS_TOKEN_HERE";

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(InvocationTargetException.class);

    private final Main plugin;
    private final BukkitContext context;

    public FastStatsManager(Main plugin) {
        this.plugin = plugin;
        this.context = new BukkitContext.Factory(plugin, FASTSTATS_TOKEN)
                .errorTrackerService(ERROR_TRACKER)
                .metrics(factory -> factory
                        .addMetric(Metric.number("registered_buckets", () -> plugin.getBucketRegistry().getRegisteredTemplates().size()))
                        .create())
                .create();
    }

    public void onEnable() {
        context.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        context.shutdown();
    }
}
