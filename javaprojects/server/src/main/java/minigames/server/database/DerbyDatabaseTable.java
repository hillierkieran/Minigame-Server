package minigames.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import minigames.server.database.DatabaseTable;
import minigames.server.database.DatabaseAccessException;
import minigames.server.database.DatabaseInitialisationException;
import minigames.server.database.DatabaseShutdownException;


/**
 * Abstract class providing default implementations for common CRUD
 * (Create, Retrieve, Update, Delete) operations for Derby database tables.
 *
 * @param <T> the type of records managed by this table
 */
public abstract class DerbyDatabaseTable<T> implements DatabaseTable<T> {

    private static final Logger logger = LogManager.getLogger(DerbyDatabaseTable.class);

    protected Database database;
    protected String tableName;


    /**
     * Constructs a new table object tied to the provided database with the given table name.
     * Also ensures the table exists in the Derby database upon creation.
     *
     * @param database   the database this table interacts with
     * @param tableName  the name of the database table
     */
    protected DerbyDatabaseTable(Database database, String tableName) {
        this.database = database;
        this.tableName = tableName;
        ensureTableExists();
    }


    // Abstract methods to be implemented by concrete table classes
    protected abstract List<Object> getPrimaryKeyValues(T record);
    protected abstract String getTableCreationSQL();
    protected abstract String getInsertSQL();
    protected abstract List<Object> getInsertValues(T record);
    protected abstract String getUpdateSQL();
    protected abstract List<Object> getUpdateSetValues(T record);
    protected abstract String getRetrieveOneSQL();
    protected abstract String getRetrieveManySQL();
    protected abstract List<Object> getRetrieveManyKeyValues(T record);
    protected abstract String getRetrieveAllSQL();
    protected abstract String getDeleteSQL();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;


    // Simple getter for the table name
    public String getTableName() { return tableName; }


    /**
     * Ensures that the table exists within the database.
     * If it does not, it attempts to create it.
     */
    @Override
    public void ensureTableExists() {
        try (
            Connection connection = database.getConnection()
        ) {
            // Check if table exists
            ResultSet resultSet = connection.getMetaData()
                .getTables(null, null, tableName, null);
            if (!resultSet.next()) {
                // If not, create it
                try (
                    PreparedStatement stmt = connection
                        .prepareStatement(getTableCreationSQL())
                ) {
                    stmt.execute();
                    logger.info("Table '{}' has been created.", tableName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error ensuring " + tableName + " table exists", e
            );
        }
    }

    // CRUD operations

    @Override
    public void create(T record) {
        executeUpdate(
            getInsertSQL(),
            getInsertValues(record)
        );
    }

    @Override
    public void update(T record) {
        executeUpdate(
            getUpdateSQL(),
            getUpdateSetValues(record)
        );
    }

    @Override
    public T retrieveOne(T key) {
        List<T> results = executeQuery(
            getRetrieveOneSQL(),
            getPrimaryKeyValues(key),
            this::mapResultSetToEntity
        );
        return (
            results.isEmpty()
            ? null
            : results.get(0)
        );
    }

    @Override
    public List<T> retrieveMany(Object key) {
        return executeQuery(
            getRetrieveManySQL(),
            getRetrieveManyKeyValues(key),
            this::mapResultSetToEntity
        );
    }

    @Override
    public List<T> retrieveAll() {
        return executeQuery(
            getRetrieveAllSQL(),
            null,
            this::mapResultSetToEntity
        );
    }

    @Override
    public void delete(T record) {
        executeUpdate(
            getDeleteSQL(),
            getPrimaryKeyValues(record)
        );
    }


    /**
     * Executes an update operation (INSERT, UPDATE, DELETE)
     * with the provided SQL and values.
     *
     * @param sql     the SQL statement to execute
     * @param values  the values to bind to the statement
     */
    private void executeUpdate(String sql, List<Object> values) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error executing SQL: " + sql, e
            );
        }
    }


    /**
     * Executes a query (SELECT) with the provided SQL, values, and mapping function.
     *
     * @param <R>      the type of records the result will be mapped to
     * @param sql      the SQL statement to execute
     * @param values   the values to bind to the statement
     * @param mapper   the function used to map the ResultSet to a list of records
     * @return a list of mapped records
     */
    private <R> List<R> executeQuery(String sql, List<Object> values, ResultSetMapper<R> mapper) {
        List<R> results = new ArrayList<>();
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error executing SQL: " + sql, e
            );
        }
        return results;
    }


    /**
     * Functional interface defining how a ResultSet row should be
     * mapped to a specific record type.
     *
     * @param <R> the type of records the result will be mapped to
     */
    @FunctionalInterface
    private interface ResultSetMapper<R> {
        R map(ResultSet rs) throws SQLException;
    }
}
