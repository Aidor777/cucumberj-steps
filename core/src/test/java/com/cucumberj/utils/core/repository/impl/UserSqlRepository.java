package com.cucumberj.utils.core.repository.impl;

import com.cucumberj.utils.core.repository.AbstractSqlRepository;
import com.cucumberj.utils.core.repository.record.User;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

public class UserSqlRepository extends AbstractSqlRepository<User> {

    private static final Map<String, Function<User, Object>> VALUE_EXTRACTOR_BY_COLUMN_NAME =
            Map.of("id", User::id, "first_name", User::firstName, "last_name", User::lastName, "email", User::email);

    public UserSqlRepository(DataSource dataSource) {
        super("users", dataSource, VALUE_EXTRACTOR_BY_COLUMN_NAME);
    }
}
