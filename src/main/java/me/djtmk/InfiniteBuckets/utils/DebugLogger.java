package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import java.util.logging.Level;

public final class DebugLogger {

    private final Main plugin;
    private boolean debugEnabled;

    public DebugLogger(Main plugin) {
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

    public void debug(String message) {
        if (debugEnabled) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (debugEnabled) {
            plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
