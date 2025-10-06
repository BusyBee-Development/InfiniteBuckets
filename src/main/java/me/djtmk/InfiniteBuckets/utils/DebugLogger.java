package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class DebugLogger {

    private final Main plugin;
    private boolean debugEnabled;

    public DebugLogger(@NotNull Main plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void reload() {
        this.loadConfig();
    }

    private void loadConfig() {
        this.debugEnabled = plugin.getConfig().getBoolean("debug-mode", false);
        debug("Debug mode " + (debugEnabled ? "enabled" : "disabled"));
    }

    public void debug(@NotNull String message) {
        if (debugEnabled) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void debug(@NotNull String message, @NotNull Throwable throwable) {
        if (debugEnabled) {
            plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
