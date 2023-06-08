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
        testEmptyUsersTable(null);
    }

    @Test
    public void insertElements_empty() {
        testEmptyUsersTable(List.of());
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

    @Test
    public void countAll_empty() {
        insertUsers(List.of());
        Assertions.assertThat(sqlRepository.countAll())
                .as("Count of users in database")
                .isZero();
    }

    @Test
    public void countAll_single() {
        insertUsers(List.of(new User(1L, "Test", "Test", "test.test@test.com")));
        Assertions.assertThat(sqlRepository.countAll())
                .as("Count of users in database")
                .isOne();
    }

    @Test
    public void countAll_triple() {
        insertUsers(List.of(
                new User(1L, "Test", "Test", "test.test@test.com"),
                new User(2L, "Also", "Test", "also.test@test.com"),
                new User(3L, "Other", "Test", "other.test@test.com")));
        Assertions.assertThat(sqlRepository.countAll())
                .as("Count of users in database")
                .isEqualTo(3);
    }

    @Test
    public void findAll_empty() {
        List<User> users = List.of();
        insertUsers(users);
        Assertions.assertThat(sqlRepository.findAll())
                .as("All users in database")
                .hasSameElementsAs(users);
    }

    @Test
    public void findAll_single() {
        List<User> users = List.of(new User(1L, "Test", "Test", "test.test@test.com"));
        insertUsers(users);
        Assertions.assertThat(sqlRepository.findAll())
                .as("All users in database")
                .hasSameElementsAs(users);
    }

    @Test
    public void findAll_triple() {
        List<User> users = List.of(
                new User(1L, "Test", "Test", "test.test@test.com"),
                new User(2L, "Also", "Test", "also.test@test.com"),
                new User(3L, "Other", "Test", "other.test@test.com"));
        insertUsers(users);
        Assertions.assertThat(sqlRepository.findAll())
                .as("All users in database")
                .hasSameElementsAs(users);
    }

    private void testEmptyUsersTable(List<User> users) {
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

    private void insertUsers(List<User> users) {
        var sqlInsertQuery = "insert into users(id, first_name, last_name, email) values (?, ?, ?, ?)";
        try (var connection = DatabaseUtils.getH2DataSource().getConnection();
                var statement = connection.prepareStatement(sqlInsertQuery)) {

            for (var user : users) {
                statement.setLong(1, user.id());
                statement.setString(2, user.firstName());
                statement.setString(3, user.lastName());
                statement.setString(4, user.email());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
