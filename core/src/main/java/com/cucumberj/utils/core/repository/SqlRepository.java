package com.cucumberj.utils.core.repository;

import java.util.Collection;

/**
 * A repository interacting with a table in a SQL database
 * @param <T> the type of the domain object representing data in the table
 */
public interface SqlRepository<T> {

    /**
     * @return the name of the table this repository interacts with
     */
    String tableName();

    /**
     * Insert a collection of elements into the database
     * @param elements the elements to insert, null-safe
     */
    void insertElements(Collection<T> elements);
}
