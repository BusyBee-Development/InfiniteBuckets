package me.djtmk.InfiniteBuckets;

import com.google.common.base.Preconditions;
import me.djtmk.InfiniteBuckets.commands.CommandManager;
import me.djtmk.InfiniteBuckets.item.BucketRegistry;
import me.djtmk.InfiniteBuckets.item.ItemEvents;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import me.djtmk.InfiniteBuckets.utils.VersionCheck;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private MessageManager messageManager;
    private BucketRegistry bucketRegistry;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.messageManager = new MessageManager(this);
        this.bucketRegistry = new BucketRegistry(this);

        new CommandManager(this);
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
        this.messageManager.reload();
        this.bucketRegistry.reload();
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BucketRegistry getBucketRegistry() {
        return bucketRegistry;
    }

    public static Main getInstance() {
        Preconditions.checkNotNull(instance, "InfiniteBuckets has not been enabled yet!");
        return instance;
    }
}
