package com.cucumberj.utils.core.repository.impl;

import com.cucumberj.utils.core.repository.AbstractSqlRepository;
import com.cucumberj.utils.core.repository.record.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

public class UserSqlRepository extends AbstractSqlRepository<User> {

    private static final Map<String, Function<User, Object>> VALUE_EXTRACTOR_BY_COLUMN_NAME =
            Map.of("id", User::id, "first_name", User::firstName, "last_name", User::lastName, "email", User::email);

    public UserSqlRepository(DataSource dataSource) {
        super("users", dataSource, VALUE_EXTRACTOR_BY_COLUMN_NAME);
    }

    @Override
    protected User mapRow(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var firstName = resultSet.getString("first_name");
        var lastName = resultSet.getString("last_name");
        var email = resultSet.getString("email");
        return new User(id, firstName, lastName, email);
    }
}
