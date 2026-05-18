package net.busybee.InfiniteBuckets.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLProvider implements DatabaseProvider {
    private final ConfigurationSection config;
    private HikariDataSource dataSource;

    public MySQLProvider(ConfigurationSection config) {
        this.config = config;
    }

    @Override
    public void initialize() throws SQLException {
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "infinitebuckets");
        String username = config.getString("username", "root");
        String password = config.getString("password", "");
        boolean useSSL = config.getBoolean("use-ssl", false);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setPoolName("InfiniteBuckets-MySQL");
        
        hikariConfig.setMaximumPoolSize(config.getInt("pool-settings.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("pool-settings.minimum-idle", 2));
        hikariConfig.setMaxLifetime(config.getLong("pool-settings.max-lifetime", 1800000));
        hikariConfig.setConnectionTimeout(config.getLong("pool-settings.connection-timeout", 5000));

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        hikariConfig.addDataSourceProperty("useSSL", String.valueOf(useSSL));

        this.dataSource = new HikariDataSource(hikariConfig);
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
        return "MySQL";
    }
}
