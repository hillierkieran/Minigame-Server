package minigames.server.database;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import minigames.server.utilities.Utilities;


/**
 * Abstract class providing default implementations for common CRUD
 * (Create, Retrieve, Update, Delete) operations for database tables.
 *
 * @param <T> the type of records managed by this table
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public abstract class DatabaseTable<T> implements DatabaseCRUDOperations<T> {

    protected final Logger logger = LogManager.getLogger(this.getClass());
    private final String BACKUP_DIR = System.getProperty("user.dir") + "/database/backup/";

    protected Database database;
    protected String tableName;
    protected String filename;

    /**
     * Constructs a new table object tied to the default database with the specified table name.
     *
     * @param tableName  the name of the database table
     */
    protected DatabaseTable(String tableName) {
        this(DatabaseConfig.getDefaultInstance(), tableName);
    }


    /**
     * Constructs a new table object tied to the specified database with the specified table name.
     *
     * @param database   the database this table interacts with
     * @param tableName  the name of the database table
     */
    protected DatabaseTable(Database database, String tableName) {
        this.database  = database;
        this.tableName = tableName;
        this.filename  = BACKUP_DIR + sanitizeFilename(tableName) + ".sql";
        this.database.registerTable(this);
        restore();
    }


    // Getters
    public String getTableName() { return tableName; }

    // Restores the table data from the default backup location.
    public synchronized void restore() {
        restore(new File(filename));
    }

    /**Backs-up the table data from the default backup location.
     * @throws IOException if backup directory creation fails. */
    public synchronized void backup() throws IOException {
        backup(new File(BACKUP_DIR));
    }


    public synchronized void createTable() {
        if (!tableExists()) {
            try {
                execute(getTableCreationSQL());
                logger.info("Table '{}' has been created.", tableName);
            } catch (DatabaseAccessException e) {
                throw new DatabaseAccessException(
                    "Error creating " + tableName, e);
            }
        }
    }


    // CRUD (Create, Retrieve, Update, Delete) operations

    @Override
    public synchronized void create(T record) {
        executeUpdate(
            getInsertSQL(),
            getInsertValues(record)
        );
    }

    @Override
    public synchronized void update(T record) {
        List<Object> combinedValues = new ArrayList<>();
        combinedValues.addAll(getUpdateSetValues(record));
        combinedValues.addAll(getPrimaryKeyValues(record));
        executeUpdate(getUpdateSQL(), combinedValues);
    }

    @Override
    public synchronized T retrieveOne(T key) {
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
    public synchronized List<T> retrieveMany(Object filterCriteria) {
        return executeQuery(
            getRetrieveManySQL(),
            getRetrieveManyKeyValues((T) filterCriteria),
            this::mapResultSetToEntity
        );
    }

    @Override
    public synchronized List<T> retrieveAll() {
        return executeQuery(
            getRetrieveAllSQL(),
            null,
            this::mapResultSetToEntity
        );
    }

    @Override
    public synchronized void delete(T record) {
        executeUpdate(
            getDeleteSQL(),
            getPrimaryKeyValues(record)
        );
    }


    public synchronized void destroyTable() {
        database.unregisterTable(this);
        if (tableExists()) {
            logger.info("Attempting to destroy table: {}", tableName);
            try {
                execute("DROP TABLE " + tableName);
                logger.info("Table '{}' has been successfully destroyed.", tableName);
            } catch (DatabaseAccessException e) {
                String message = "Error while destroying table " + tableName;
                logger.error(message, e);
                throw new DatabaseAccessException(message, e);
            }
        }
    }


    // Abstract methods to be implemented by extended table classes
    protected abstract List<Object> getPrimaryKeyValues(T record);
    protected abstract String getTableCreationSQL();
    protected abstract String getInsertSQL();
    protected abstract List<Object> getInsertValues(T record);
    protected abstract String getUpdateSQL();
    protected abstract List<Object> getUpdateSetValues(T record);
    protected abstract String getRetrieveOneSQL();
    protected abstract String getRetrieveManySQL();
    protected abstract List<Object> getRetrieveManyKeyValues(Object filterCriteria);
    protected abstract String getRetrieveAllSQL();
    protected abstract String getDeleteSQL();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;


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


    // Replaces invalid characters in the table name to produce a valid filename
    private String sanitizeFilename(String tableName) {
        return tableName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    // Constructs the SQL command to restore data from the backup file.
    private String getRestoreCommand() {
        return getRestoreCommand(tableName, filename);
    }

    // Constructs the SQL command to restore data from the backup file.
    private String getRestoreCommand(String tableName, String filename) {
        return (
            "CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (" +
                "null,'" +          // Schema name (use default)
                tableName + "', '" +// Table name
                filename + "', " +  // Filename
                "null, " +          // Column delimiter (use default: ',' )
                "null, " +          // Character delimiter (use default: '"'
                "null, " +          // Code-set (use default)
                "0)"                // Mode: INSERT (ie, don't overwrite)
        );
    }

    // Constructs the SQL command to backup the current table data.
    private String getBackupCommand() {
        return getBackupCommand(tableName, filename);
    }

    // Constructs the SQL command to backup the current table data.
    private String getBackupCommand(String tableName, String filename) {
        return (
            "CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (" +
                "null, '" +         // Schema name (use default)
                tableName + "', '" +// Table name
                filename + "', "+   // Filename
                "null, " +          // Column delimiter (use default: ',' )
                "null, " +          // Character delimiter (use default: '"' )
                "null)"             // Code-set (use default)
        );
    }

    // Restores table data from the specified backup file.
    void restore(File file) {
        if (file.exists()) {
            String tempTableName = tableName + "_temp";
            // restore to temporary table
            execute(getRestoreCommand(tempTableName, file.getAbsolutePath()));
            if (hasDuplicates(tempTableName)) {
                logger.error("Backup contains duplicates and cannot be restored.");
            } else {
                // Append Data from the Temporary Table
                execute("INSERT INTO " + tableName + " SELECT * FROM " + tempTableName);
                logger.info("Data from backup appended to table: {}", tableName);
            }
            // Drop the temporary table
            execute("DROP TABLE " + tempTableName);
        }
    }

    private boolean hasDuplicates(String tempTableName) {
        String checkDuplicatesSQL = String.format(
            // Assuming the primary key column is id
            "SELECT COUNT(*) FROM %s WHERE id IN (SELECT id FROM %s)",
            tableName, tempTableName
        );
    
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(checkDuplicatesSQL);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking duplicates for table: {}", tableName, e);
        }
        return false;
    }


    // Initiates backup to the provided directory.
    void backup(File dir) throws IOException {
        if (tableExists()) {
            if (!dir.exists() && !dir.mkdirs()) {
                // Could not create the directory
                logger.error("Failed to create backup directory: {}", dir);
                throw new IOException("Unable to create backup directory.");
            }
            execute(getBackupCommand());
        }
    }


    /**
     * Checks whether the represented table exists in the database.
     * 
     * @return true if table exists, otherwise false.
     */
    public synchronized boolean tableExists() {
        try (
            Connection connection = database.getConnection();
            ResultSet resultSet = connection.getMetaData()
                .getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" });
        ) {
            return resultSet.next();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error while checking if " + tableName + " table exists", e);
        }
    }

    /**
     * Ensures that the represented table exists in the database.
     */
    public synchronized void ensureTableExists() {
        if (!tableExists()) {
            createTable();
        }
    }


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
     * Executes the provided SQL command. Primarily used for non-CRUD operations 
     * like backup and restore.
     *
     * @param sql the SQL command to be executed
     */
    private synchronized void execute(String sql) {
        executeTransactional(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.execute();
                if (database.isTest()) {
                    logger.info("Executing SQL: " + sql);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    /**
     * Executes an update operation (INSERT, UPDATE, DELETE)
     * with the provided SQL and values.
     *
     * @param sql     the SQL statement to execute
     * @param values  the values to bind to the statement
     */
    private synchronized void executeUpdate(String sql, List<Object> values) {
        ensureTableExists();
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
                if (database.isTest()) {
                    logger.info("Executing SQL: " + logSQL.toString());
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
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
    private synchronized <R> List<R> executeQuery(String sql, List<Object> values, ResultSetMapper<R> mapper) {
        ensureTableExists();
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
                if (database.isTest()) {
                    logger.info("Executing SQL: " + logSQL.toString());
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        return results;
    }
}
