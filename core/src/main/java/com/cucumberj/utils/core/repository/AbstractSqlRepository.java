package com.cucumberj.utils.core.repository;

import com.cucumberj.utils.core.model.UniquelyIdentified;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlRepository<T extends UniquelyIdentified> implements SqlRepository<T> {

    protected final String tableName;

    protected final DataSource dataSource;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Function<T, Object>> valueExtractorByColumnName;

    private final String sqlInsertQuery;

    /**
     * @param tableName the name of the table this repository interacts with
     * @param dataSource a datasource to connect to the database
     * @param valueExtractorByColumnName a map with keys being the name of the column, and values being functions extracting the corresponding data from the domain object
     */
    protected AbstractSqlRepository(
            String tableName, DataSource dataSource, Map<String, Function<T, Object>> valueExtractorByColumnName) {
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.valueExtractorByColumnName = new LinkedHashMap<>(valueExtractorByColumnName);
        this.sqlInsertQuery = buildSqlInsertQuery(this.valueExtractorByColumnName.keySet());
    }

    private String buildSqlInsertQuery(Set<String> columnNames) {
        var result = new StringBuilder("insert into ").append(tableName).append(" (");
        result.append(String.join(", ", columnNames));
        result.append(") ").append("values").append(" (");
        result.append(columnNames.stream().map(colName -> "?").collect(Collectors.joining(", ")));
        result.append(")");
        logger.debug("Insert query built: " + result);
        return result.toString();
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public void insertElements(Collection<T> elements) {
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sqlInsertQuery)) {

            for (T element : CollectionUtils.emptyIfNull(elements)) {
                int i = 1;

                for (var extractor : valueExtractorByColumnName.values()) {
                    statement.setObject(i, extractor.apply(element));
                    i++;
                }
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            logger.error("Error while inserting elements into table " + tableName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countAll() {
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("select count(*) from " + tableName())) {

            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            logger.error("Error while counting elements in table " + tableName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> elements = new LinkedList<>();
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("select * from " + tableName())) {

            while (resultSet.next()) {
                elements.add(mapRow(resultSet));
            }
        } catch (Exception e) {
            logger.error("Error while fetching elements from table " + tableName(), e);
            throw new RuntimeException(e);
        }
        return elements;
    }

    /**
     * Map a row int the database to a domain object
     *
     * @param resultSet the result set at the next position
     * @return a domain object representing a row in the database
     */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;
}
