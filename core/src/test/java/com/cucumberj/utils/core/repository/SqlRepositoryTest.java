package com.cucumberj.utils.core.repository;

import com.cucumberj.utils.core.repository.datasource.DatabaseUtils;
import com.cucumberj.utils.core.repository.impl.UserSqlRepository;
import com.cucumberj.utils.core.repository.record.User;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlRepositoryTest {

    private SqlRepository<User> sqlRepository;

    @BeforeAll
    public static void init() {
        DatabaseUtils.initDB();
    }

    @BeforeEach
    public void reset() {
        DatabaseUtils.resetDB();
        sqlRepository = new UserSqlRepository(DatabaseUtils.getH2DataSource());
    }

    @Test
    public void insertElements_null() {
        testEmptyDatabase(null);
    }

    @Test
    public void insertElements_empty() {
        testEmptyDatabase(List.of());
    }

    @Test
    public void insertElements_single() {
        testInsertedUsers(List.of(new User(1L, "Test", "Test", "test.test@test.com")));
    }

    @Test
    public void insertElements_several() {
        testInsertedUsers(List.of(
                new User(1L, "Test", "Test", "test.test@test.com"),
                new User(2L, "Also", "Test", "also.test@test.com"),
                new User(3L, "Other", "Test", "other.test@test.com")));
    }

    private void testEmptyDatabase(List<User> users) {
        sqlRepository.insertElements(users);
        try (var connection = DatabaseUtils.getH2DataSource().getConnection();
                var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery("select count(*) from " + sqlRepository.tableName());
            resultSet.next();
            Assertions.assertThat(resultSet.getInt(1))
                    .as("Count of elements found in DB")
                    .isZero();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void testInsertedUsers(List<User> users) {
        sqlRepository.insertElements(users);
        try (var connection = DatabaseUtils.getH2DataSource().getConnection();
                var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery("select * from " + sqlRepository.tableName());
            var queriedUsers = new LinkedList<User>();

            while (resultSet.next()) {
                queriedUsers.add(new User(
                        resultSet.getLong("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email")));
            }

            Assertions.assertThat(queriedUsers).as("Elements found in DB").hasSameElementsAs(users);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
