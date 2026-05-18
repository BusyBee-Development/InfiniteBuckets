package net.busybee.InfiniteBuckets.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.busybee.InfiniteBuckets.Main;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteProvider implements DatabaseProvider {
    private final Main plugin;
    private HikariDataSource dataSource;

    public SQLiteProvider(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "database.db");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("InfiniteBuckets-SQLite");
        config.setMaximumPoolSize(1); // SQLite only supports one writer at a time
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("DataSource is not initialized");
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public String getType() {
        return "SQLite";
    }
}
