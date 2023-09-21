package minigames.server.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Default CRUD operations for database tables.
 * 
 * @param <T> Type of records managed by the table
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public abstract class DatabaseTable<T> implements DatabaseCRUDOperations<T> {

    protected final Logger logger = LogManager.getLogger(this.getClass());
    private static final String BACKUP_DIR = System.getProperty("user.dir") + "/database/backup/";

    protected Database database;
    protected String filePath;
    protected String tableName;


// Constructors

    /**
     * Constructor.
     * @param tableName Name of the database table.
     */
    protected DatabaseTable(String tableName) {
        this(Database.getInstance(), tableName);
    }

    /**
     * Constructor.
     * @param database Database this table interacts with.
     * @param tableName Name of the database table.
     */
    protected DatabaseTable(Database database, String tableName) {
        this.database  = database;
        this.tableName = tableName.toUpperCase();
        this.filePath  = BACKUP_DIR + this.tableName + ".sql";
        this.database.registerTable(this);
        restore();
    }


// Getters

    public String getTableName() { return tableName; }


// CRUD (Create, Retrieve, Update, Delete) functions

    @Override // Inserts a new record.
    public synchronized void create(T record) {
        executeUpdate(getInsertSQL(), getInsertValues(record));
    }

    @Override // Modifies an existing record.
    public synchronized void update(T record) {
        executeUpdate(
            getUpdateSQL(),
            Stream.concat(
                getUpdateSetValues(record).stream(),
                getPrimaryKeyValues((Object) record).stream()
            ).collect(Collectors.toList())
        );
    }

    @Override // Fetches a single record based on criteria.
    public synchronized T retrieveOne(Object filterCriteria) {
        List<T> results = executeQuery(getRetrieveOneSQL(), getPrimaryKeyValues(filterCriteria), this::mapResultSetToEntity);
        return (results.isEmpty() ? null : results.get(0));
    }

    @Override // Fetches multiple records based on criteria.
    public synchronized List<T> retrieveMany(Object filterCriteria) {
        return executeQuery(getRetrieveManySQL(), getRetrieveManyKeyValues(filterCriteria), this::mapResultSetToEntity);
    }

    @Override // Fetches all records.
    public synchronized List<T> retrieveAll() {
        return executeQuery(getRetrieveAllSQL(), null, this::mapResultSetToEntity);
    }

    @Override // Removes a specific record.
    public synchronized void delete(T record) {
        executeUpdate(getDeleteSQL(), getPrimaryKeyValues((Object) record));
    }


// Table management functions

    /**
     * Creates the table in the database.
     */
    public synchronized void createTable() {
        createTable(tableName);
    }
    synchronized void createTable(String tableName) {
        if (!tableExists(tableName))
            execute(getTableCreationSQL().replace(this.tableName, tableName));
    }

    /**
     * Removes all records from the table.
     */
    public synchronized void clearTable() {
        clearTable(tableName);
    }
    synchronized void clearTable(String tableName) {
        if (tableExists(tableName)) execute("DELETE FROM " + tableName);
    }

    /**
     * Deletes the table from the database.
     */
    public synchronized void destroyTable() {
        destroyTable(tableName);
    }
    synchronized void destroyTable(String tableName) {
        database.unregisterTable(this);
        if (tableExists(tableName)) execute("DROP TABLE " + tableName);
    }

    /**
     * Checks if the table exists in the database.
     *
     * @return True if table exists, false otherwise.
     */
    public synchronized boolean tableExists() {
        return tableExists(tableName);
    }
    synchronized boolean tableExists(String tableName) {
        try(Connection connection = database.getConnection();
            ResultSet resultSet = connection.getMetaData().getTables(
                null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return resultSet.next();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Error", e);
        }
    }


// Backup and Restore functions

    /**
     * Backs up the table to the default directory.
     *
     * @throws IOException If backup fails.
     */
    public synchronized void backup() throws IOException {
        backup(new File(BACKUP_DIR));
    }
    synchronized void backup(File dir) throws IOException {
        if (tableExists()) {
            if (!dir.exists() && !dir.mkdirs())
                throw new IOException("Unable to create backup directory.");
            String tempfilePath = filePath.replace(".sql", "_temp.sql");
            execute(getBackupCommand(tableName, tempfilePath)); // backup to temp file
            new File(filePath).delete(); // delete prev backup file
            new File(tempfilePath).renameTo(new File(filePath)); // rename temp
        }
    }

    /**
     * Restores the table from the default backup.
     */
    public synchronized void restore() {
        restore(new File(filePath));
    }
    synchronized void restore(File file) {
        restore(tableName, file);
    }
    synchronized void restore(String tableName, File file) {
        if (file.exists()) {
            if (tableExists()) clearTable();
            else createTable();
            execute(getRestoreCommand(tableName, BACKUP_DIR + file.getName()));
        }
    }


// Abstract methods

    /** @return List of column names in the table. */
    public abstract List<String> getColumnNames();

    /** @return List of primary key column names. */
    public abstract List<String> getKeyColumnNames();

    /** Gets the primary key values for the provided object.
     *  @param obj Object to retrieve primary key values from.
     *  @return List of primary key values. */
    protected abstract List<Object> getPrimaryKeyValues(Object obj);

    /** @return SQL string to create the table. */
    protected abstract String getTableCreationSQL();

    /** @return SQL string for insert operations. */
    protected abstract String getInsertSQL();

    /** Gets the values to be inserted into the table for a record.
     *  @param record Record to get values from.
     *  @return List of values for insert operations. */
    protected abstract List<Object> getInsertValues(T record);

    /** @return SQL string for update operations. */
    protected abstract String getUpdateSQL();

    /** Gets the values to be modified during an update operation.
     *  @param record Record to get values from.
     *  @return List of values for update operations. */
    protected abstract List<Object> getUpdateSetValues(T record);

    /** @return SQL string to retrieve a single record. */
    protected abstract String getRetrieveOneSQL();

    /** @return SQL string to retrieve multiple records based on a criterion. */
    protected abstract String getRetrieveManySQL();

    /** Gets values used for filtering in retrieveMany operations.
     *  @param filterCriteria Criteria to filter records.
     *  @return List of key values for filtering. */
    protected abstract List<Object> getRetrieveManyKeyValues(Object filterCriteria);

    /** @return SQL string to retrieve all records. */
    protected abstract String getRetrieveAllSQL();

    /** @return SQL string for delete operations. */
    protected abstract String getDeleteSQL();

    /** Maps a ResultSet row to an entity of type T.
     *  @param rs ResultSet row to map.
     *  @return Mapped entity of type T.
     *  @throws SQLException If mapping fails. */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;


// Helper functions

    /**
     * Executes an arbitrary SQL command.
     *
     * @param sql SQL command to execute.
     */
    private synchronized void execute(String sql) {
        executeTransactional(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                if (database.isTest())
                    logger.info("Executing SQL: " + sql);
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }


    /**
     * Executes a SQL update command.
     *
     * @param sql SQL update command.
     * @param values Values to bind to the SQL statement.
     */
    private synchronized void executeUpdate(String sql, List<Object> values) {
        createTable();
        executeTransactional(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                StringBuilder logSQL = new StringBuilder(sql);
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                        int index = logSQL.indexOf("?");
                        if (index != -1) {
                            logSQL.replace(index, index+1, values.get(i).toString());
                        }
                    }
                }
                if (database.isTest())
                    logger.info("Executing SQL: " + logSQL.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }


    /**
     * Executes a SQL query and maps results.
     *
     * @param <R> Result type.
     * @param sql SQL query.
     * @param values Values to bind to the SQL statement.
     * @param mapper Function to map results.
     * @return List of mapped results.
     */
    private synchronized <R> List<R> executeQuery(String sql, List<Object> values, ResultSetMapper<R> mapper) {
        createTable();
        List<R> results = new ArrayList<>();
        executeTransactional(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                StringBuilder logSQL = new StringBuilder(sql);
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                        int index = logSQL.indexOf("?");
                        if (index != -1) {
                            logSQL.replace(index, index+1, values.get(i).toString());
                        }
                    }
                }
                if (database.isTest())
                    logger.info("Executing SQL: " + logSQL.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) results.add(mapper.map(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        return results;
    }

    /**
     * Executes a SQL operation within a transaction.
     *
     * @param operation SQL operation to execute.
     */
    private void executeTransactional(Function<Connection, Void> operation) {
        Connection connection = null;
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false); // Turn off auto-commit
            operation.apply(connection); // Execute the operation
            connection.commit(); // Explicitly commit the transaction
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback if there's an error.
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                    throw new DatabaseAccessException("Error rolling back transaction", rollbackEx);
                }
            }
            throw new DatabaseAccessException("Error executing SQL", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Reset auto-commit.
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection after transaction", closeEx);
                    throw new DatabaseAccessException("Error closing connection after transaction", closeEx);
                }
            }
        }
    }


    /**
     * Defines how a ResultSet row should be mapped to
     * a specific output record object class.
     *
     * @param <R> the type of record object the result will be mapped to.
     */
    @FunctionalInterface
    private interface ResultSetMapper<R> {
        R map(ResultSet rs) throws SQLException;
    }


    // Constructs the SQL command to restore data from the backup file.
    private String getRestoreCommand() {
        return getRestoreCommand(tableName, filePath);
    }
    private String getRestoreCommand(String tableName, String filePath) {
        return (
            "CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (" +
                "null,'" +          // Schema name (use default)
                tableName + "', '" +// Table name
                filePath + "', " +  // filePath
                "null, " +          // Column delimiter (use default: ',' )
                "null, " +          // Character delimiter (use default: '"'
                "null, " +          // Code-set (use default)
                "1)"                // Mode: REPLACE (ie, overwrite)
        );
    }


    // Constructs the SQL command to backup the current table data.
    private String getBackupCommand() {
        return getBackupCommand(tableName, filePath);
    }
    private String getBackupCommand(String tableName, String filePath) {
        return (
            "CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (" +
                "null, '" +         // Schema name (use default)
                tableName + "', '" +// Table name
                filePath + "', "+   // filePath
                "null, " +          // Column delimiter (use default: ',' )
                "null, " +          // Character delimiter (use default: '"' )
                "null)"             // Code-set (use default)
        );
    }
}
