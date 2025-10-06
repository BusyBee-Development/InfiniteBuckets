package me.djtmk.InfiniteBuckets;

import com.google.common.base.Preconditions;
import me.djtmk.InfiniteBuckets.commands.InfiniteBucketsCommand;
import me.djtmk.InfiniteBuckets.hooks.HookManager;
import me.djtmk.InfiniteBuckets.item.BucketRegistry;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.utils.DebugLogger;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import me.djtmk.InfiniteBuckets.utils.VersionCheck;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private MessageManager messageManager;
    private BucketRegistry bucketRegistry;
    private DebugLogger debugLogger;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config files
        this.saveDefaultConfig();
        if (!new java.io.File(this.getDataFolder(), "buckets.yml").exists()) {
            this.saveResource("buckets.yml", false);
        }
        
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

        this.getLogger().info("InfiniteBuckets v" + this.getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("InfiniteBuckets has been disabled.");
    }

    public void reload() {
        this.reloadConfig();
        this.debugLogger.reload();
        this.messageManager.reload();
        this.bucketRegistry.reload();
        this.hookManager = new HookManager(this);
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

    public static Main getInstance() {
        Preconditions.checkNotNull(instance, "InfiniteBuckets has not been enabled yet!");
        return instance;
    }
}
