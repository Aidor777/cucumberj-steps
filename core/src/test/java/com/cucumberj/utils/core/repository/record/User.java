package com.cucumberj.utils.core.repository.record;

import com.cucumberj.utils.core.model.UniquelyIdentified;

public record User(Long id, String firstName, String lastName, String email) implements UniquelyIdentified {

    @Override
    public String uniqueKey() {
        return email;
    }
}
