package com.cucumberj.utils.core.repository.datasource;

import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

public final class DatabaseUtils {

    private static DataSource H2_DATA_SOURCE = null;

    public static DataSource getH2DataSource() {
        if (H2_DATA_SOURCE != null) {
            return H2_DATA_SOURCE;
        }

        var h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:test");
        h2DataSource.setUser("Test");
        h2DataSource.setPassword("");

        var connectionPool = JdbcConnectionPool.create(h2DataSource);
        H2_DATA_SOURCE = connectionPool;
        return connectionPool;
    }

    public static void initDB() {
        try (var connection = getH2DataSource().getConnection()) {
            var scriptReader = new InputStreamReader(
                    Objects.requireNonNull(DatabaseUtils.class.getClassLoader().getResourceAsStream("schema.sql")));
            RunScript.execute(connection, scriptReader);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetDB() {
        try (var connection = getH2DataSource().getConnection();
                var statement = connection.createStatement()) {
            statement.execute("set referential_integrity false");

            var resultSet = statement.executeQuery(
                    "select table_name from information_schema.tables where table_schema = 'PUBLIC'");
            var tableNames = new HashSet<String>();
            while (resultSet.next()) {
                var tableName = resultSet.getString(1);
                tableNames.add(tableName);
            }

            for (String table : tableNames) {
                statement.execute("truncate table " + table);
            }

            statement.execute("set referential_integrity true");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
