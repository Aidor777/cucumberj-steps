package com.cucumberj.utils.core.repository;

import com.cucumberj.utils.core.model.UniquelyIdentified;
import java.util.Collection;
import java.util.List;

/**
 * A repository interacting with a table in a SQL database
 * @param <T> the type of the domain object representing data in the table, should have a unique String key identifier
 */
public interface SqlRepository<T extends UniquelyIdentified> {

    /**
     * @return the name of the table this repository interacts with
     */
    String tableName();

    /**
     * Insert a collection of elements into the database
     * @param elements the elements to insert, null-safe
     */
    void insertElements(Collection<T> elements);

    /**
     * @return the count of all records in the table
     */
    int countAll();

    /**
     * @return all records found in the table
     */
    List<T> findAll();
}
