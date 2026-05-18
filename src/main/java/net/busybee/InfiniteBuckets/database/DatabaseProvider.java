package net.busybee.InfiniteBuckets.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseProvider {
    void initialize() throws SQLException;
    Connection getConnection() throws SQLException;
    void close();
    String getType();
}
