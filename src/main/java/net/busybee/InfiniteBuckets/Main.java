package net.busybee.InfiniteBuckets;

import com.google.common.base.Preconditions;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.commands.InfiniteBucketsCommand;
import net.busybee.InfiniteBuckets.core.ConfigManager;
import net.busybee.InfiniteBuckets.core.PluginLifecycle;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.item.BucketRegistry;
import net.busybee.InfiniteBuckets.item.ItemEvents;
import net.busybee.InfiniteBuckets.utils.DebugLogger;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.busybee.InfiniteBuckets.utils.VersionCheck;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static PlatformScheduler scheduler;

    private final PluginLifecycle lifecycle = new PluginLifecycle();
    private ConfigManager configManager;
    private MessageManager messageManager;
    private BucketRegistry bucketRegistry;
    private DebugLogger debugLogger;
    private HookManager hookManager;
    private ExecutorService asyncExecutor;

    public static PlatformScheduler scheduler() {
      return scheduler;
    }

    @Override
    public void onEnable() {
        instance = this;
        lifecycle.beginStartup();

        FoliaLib foliaLib = new FoliaLib(this);
        scheduler = foliaLib.getScheduler();
        asyncExecutor = Executors.newFixedThreadPool(4, new NamedThreadFactory());

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.debugLogger = new DebugLogger(this);
        this.messageManager = new MessageManager(this);
        this.bucketRegistry = new BucketRegistry(this);
        this.hookManager = new HookManager(this);

        PluginCommand command = this.getCommand("infinitebuckets");
        if (command != null) {
            InfiniteBucketsCommand infiniteBucketsCommand = new InfiniteBucketsCommand(this);
            command.setExecutor(infiniteBucketsCommand);
            command.setTabCompleter(infiniteBucketsCommand);
            debugLogger.debug("Registered command 'infinitebuckets' to InfiniteBucketsCommand");
        } else {
            this.getLogger().severe("Could not register command 'infinitebuckets'! Please ensure it is in your plugin.yml");
        }

        this.getServer().getPluginManager().registerEvents(new ItemEvents(this), this);
        this.getServer().getPluginManager().registerEvents(new VersionCheck(this), this);

        // Initialize bStats metrics
        new Metrics(this, 28821);

        lifecycle.markRunning();
        this.getLogger().info("InfiniteBuckets v" + this.getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        lifecycle.beginShutdown();

        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
            }
        }

        lifecycle.markStopped();
        this.getLogger().info("InfiniteBuckets has been disabled.");
    }

    public void reload() {
        this.configManager.reload();
        this.debugLogger.reload();
        this.messageManager.reload();
        this.bucketRegistry.reload();
        this.hookManager = new HookManager(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    public MessageManager getMessageManager() {
        return messageManager;
    }
    public BucketRegistry getBucketRegistry() {
        return bucketRegistry;
    }
    public DebugLogger getDebugLogger() {
        return debugLogger;
    }
    public HookManager getHookManager() {
        return hookManager;
    }
    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public static Main getInstance() {
        Preconditions.checkNotNull(instance, "InfiniteBuckets has not been enabled yet!");
        return instance;
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadId = new AtomicInteger(1);

        @Override
        public @NotNull Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "InfiniteBuckets-Async-" + threadId.getAndIncrement());
        }
    }
}
