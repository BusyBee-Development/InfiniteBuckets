package me.djtmk.InfiniteBuckets;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginContext;
import com.hypixel.hytale.server.core.HytaleServer;
import me.djtmk.InfiniteBuckets.commands.InfBCommand;
import me.djtmk.InfiniteBuckets.listeners.BucketListener;

public class InfiniteBuckets extends JavaPlugin {

    private static InfiniteBuckets instance;

    public InfiniteBuckets(PluginContext context) {
        super(context);
    }

    @Override
    public void onEnable() {
        instance = this;

        // Register Command
        HytaleServer.get().getCommandManager().registerCommand(new InfBCommand(this));

        // Register ECS System
        HytaleServer.get().getSystemRegistry().registerSystem(new BucketListener(this));

        getLogger().info("InfiniteBuckets has been enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static InfiniteBuckets getInstance() {
        return instance;
    }
}
