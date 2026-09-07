package net.busybee.InfiniteBuckets;

import com.google.common.base.Preconditions;
import fr.mrmicky.fastinv.FastInvManager;
import net.busybee.InfiniteBuckets.commands.InfiniteBucketsCommand;
import net.busybee.InfiniteBuckets.core.ConfigManager;
import net.busybee.InfiniteBuckets.core.PluginLifecycle;
import net.busybee.InfiniteBuckets.database.DatabaseManager;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.inventory.impl.ChatPromptListener;
import net.busybee.InfiniteBuckets.item.BucketProtectionListener;
import net.busybee.InfiniteBuckets.item.BucketRegistry;
import net.busybee.InfiniteBuckets.item.BucketUseListener;
import net.busybee.InfiniteBuckets.item.BucketVanillaGuardListener;
import net.busybee.InfiniteBuckets.scheduling.BucketScheduler;
import net.busybee.InfiniteBuckets.utils.BStatsManager;
import net.busybee.InfiniteBuckets.utils.DebugLogger;
import net.busybee.InfiniteBuckets.utils.FastStatsManager;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.busybee.InfiniteBuckets.utils.VersionCheck;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    private final PluginLifecycle lifecycle = new PluginLifecycle();
    private BucketScheduler bucketScheduler;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private BucketRegistry bucketRegistry;
    private DatabaseManager databaseManager;
    private DebugLogger debugLogger;
    private HookManager hookManager;
    private FastStatsManager fastStatsManager;

    @Override
    public void onEnable() {
        instance = this;
        lifecycle.beginStartup();

        FastInvManager.register(this);

        this.bucketScheduler = new BucketScheduler(this);

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.debugLogger = new DebugLogger(this);
        this.messageManager = new MessageManager(this);

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

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

        this.getServer().getPluginManager().registerEvents(new BucketUseListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BucketProtectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BucketVanillaGuardListener(this), this);
        this.getServer().getPluginManager().registerEvents(new VersionCheck(this), this);
        this.getServer().getPluginManager().registerEvents(new ChatPromptListener(this), this);

        new BStatsManager(this);
        this.fastStatsManager = new FastStatsManager(this);
        this.fastStatsManager.onEnable();

        lifecycle.markRunning();
        this.getLogger().info("InfiniteBuckets v" + this.getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        lifecycle.beginShutdown();

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (fastStatsManager != null) {
            fastStatsManager.onDisable();
        }

        lifecycle.markStopped();
        this.getLogger().info("InfiniteBuckets has been disabled.");
    }

    public void reload() {
        this.configManager.reload();
        this.debugLogger.reload();
        this.messageManager.reload();

        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        this.bucketRegistry.reload();
        this.hookManager.reload();
    }

    public PluginLifecycle getLifecycle() {
        return lifecycle;
    }
    public BucketScheduler getBucketScheduler() {
        return bucketScheduler;
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
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public DebugLogger getDebugLogger() {
        return debugLogger;
    }
    public HookManager getHookManager() {
        return hookManager;
    }

    public static Main getInstance() {
        Preconditions.checkNotNull(instance, "InfiniteBuckets has not been enabled yet!");
        return instance;
    }
}
