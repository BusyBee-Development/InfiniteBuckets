package net.busybee.InfiniteBuckets.database;

import net.busybee.InfiniteBuckets.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private final Main plugin;
    private DatabaseProvider provider;
    private final Map<String, Long> cooldownCache = new ConcurrentHashMap<>();

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("database");
        if (config == null) {
            plugin.getLogger().warning("Database section missing in config.yml! Defaulting to SQLite.");
            provider = new SQLiteProvider(plugin);
        } else {
            String type = config.getString("type", "SQLITE").toUpperCase();
            if (type.equals("MYSQL")) {
                provider = new MySQLProvider(config.getConfigurationSection("mysql"));
            } else {
                provider = new SQLiteProvider(plugin);
            }
        }

        try {
            provider.initialize();
            plugin.getLogger().info("Successfully initialized " + provider.getType() + " database.");
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database!");
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_cooldowns (" +
                             "uuid VARCHAR(36) NOT NULL, " +
                             "bucket_id VARCHAR(64) NOT NULL, " +
                             "expiry LONG NOT NULL, " +
                             "PRIMARY KEY (uuid, bucket_id))")) {
            ps.executeUpdate();
        }
    }

    public void close() {
        if (provider != null) {
            provider.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return provider.getConnection();
    }

    public CompletableFuture<Long> getCooldown(UUID uuid, String bucketId) {
        String key = uuid.toString() + ":" + bucketId;
        Long cached = cooldownCache.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            if (plugin.getLifecycle().isStoppingOrStopped()) return 0L;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT expiry FROM player_cooldowns WHERE uuid = ? AND bucket_id = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, bucketId);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    long expiry = rs.getLong("expiry");
                    cooldownCache.put(key, expiry);
                    return expiry;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0L;
        });
    }

    public void setCooldown(UUID uuid, String bucketId, long expiry) {
        String key = uuid.toString() + ":" + bucketId;
        cooldownCache.put(key, expiry);

        CompletableFuture.runAsync(() -> {
            if (plugin.getLifecycle().isStoppingOrStopped()) return;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "REPLACE INTO player_cooldowns (uuid, bucket_id, expiry) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, bucketId);
                ps.setLong(3, expiry);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
