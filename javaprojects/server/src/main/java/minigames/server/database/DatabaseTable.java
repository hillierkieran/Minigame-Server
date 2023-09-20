package minigames.server.database;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final String BACKUP_DIR = System.getProperty("user.dir") + "/database/backup/";

    protected Database database;
    protected String backupDir;
    protected String filename;
    protected String tableName;

    /**
     * Constructs a new table object tied to the default database with the specified table name.
     *
     * @param tableName  the name of the database table
     */
    protected DatabaseTable(String tableName) {
        this(Database.getInstance(), tableName);
    }


    /**
     * Constructs a new table object tied to the specified database with the specified table name.
     *
     * @param database   the database this table interacts with
     * @param tableName  the name of the database table
     */
    protected DatabaseTable(Database database, String tableName) {
        this.database  = database;
        this.backupDir = BACKUP_DIR;
        this.tableName = sanitizeString(tableName).toUpperCase();
        this.filename  = this.backupDir + this.tableName + ".sql";
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
        backup(new File(backupDir));
    }


    public synchronized void createTable() {
        createTable(tableName);
    }


    public synchronized void createTable(String tableName) {
        if (!tableExists(tableName)) {
            if (database.isTest()) {
                logger.info("Attempting to create table '{}'...", tableName);
            }
            String createTableSQL = getTableCreationSQL();
            // Use specified table name
            if (!this.tableName.equals(tableName)) {
                createTableSQL = createTableSQL.replace(
                    this.tableName, tableName);
            }
            try {
                execute(createTableSQL);
                if (database.isTest()) {
                    logger.info("Table '{}' has been created.", tableName);
                }
            } catch (DatabaseAccessException e) {
                throw new DatabaseAccessException(
                    "Error creating " + tableName, e);
            }
        }
    }


    // CRUD (Create, Retrieve, Update, Delete) operations

    @Override
    public synchronized void create(T record) {
        if (database.isTest()) {
            logger.info("Attempting to create new record in table '{}'...", tableName);
        }
        executeUpdate(
            getInsertSQL(),
            getInsertValues(record)
        );
    }

    @Override
    public synchronized void update(T record) {
        if (database.isTest()) {
            logger.info("Attempting to update record in table '{}'...", tableName);
        }
        List<Object> combinedValues = new ArrayList<>();
        combinedValues.addAll(getUpdateSetValues(record));
        combinedValues.addAll(getPrimaryKeyValues((Object) record));
        executeUpdate(getUpdateSQL(), combinedValues);
    }

    @Override
    public synchronized T retrieveOne(Object filterCriteria) {
        if (database.isTest()) {
            logger.info("Attempting to get a record from table '{}'...", tableName);
        }
        List<T> results = executeQuery(
            getRetrieveOneSQL(),
            getPrimaryKeyValues(filterCriteria),
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
        if (database.isTest()) {
            logger.info("Attempting to get records from table '{}'...", tableName);
        }
        return executeQuery(
            getRetrieveManySQL(),
            getRetrieveManyKeyValues(filterCriteria),
            this::mapResultSetToEntity
        );
    }

    @Override
    public synchronized List<T> retrieveAll() {
        if (database.isTest()) {
            logger.info("Attempting to get all records from table '{}'...", tableName);
        }
        return executeQuery(
            getRetrieveAllSQL(),
            null,
            this::mapResultSetToEntity
        );
    }

    @Override
    public synchronized void delete(T record) {
        if (database.isTest()) {
            logger.info("Attempting to delete record from table '{}'...", tableName);
        }
        executeUpdate(
            getDeleteSQL(),
            getPrimaryKeyValues((Object) record)
        );
    }


    public synchronized void clearTable() {
        clearTable(tableName);
    }


    public synchronized void clearTable(String tableName) {
        if (tableExists(tableName)) {
            if (database.isTest()) {
                logger.info("Attempting to clear all records from table '{}'...", tableName);
            }
            try {
                execute("DELETE FROM " + tableName);
                if (database.isTest()) {
                    logger.info("Table '{}' has been successfully cleared.", tableName);
                }
            } catch (DatabaseAccessException e) {
                String message = "Error while clearing table " + tableName;
                logger.error(message, e);
                throw new DatabaseAccessException(message, e);
            }
        }
    }


    public synchronized void destroyTable() {
        destroyTable(tableName);
    }


    public synchronized void destroyTable(String tableName) {
        database.unregisterTable(this);
        if (tableExists(tableName)) {
            if (database.isTest()) {
                logger.info("Attempting to destroy table '{}'...", tableName);
            }
            try {
                execute("DROP TABLE " + tableName);
                if (database.isTest()) {
                    logger.info("Table '{}' has been successfully destroyed.", tableName);
                }
            } catch (DatabaseAccessException e) {
                String message = "Error while destroying table " + tableName;
                logger.error(message, e);
                throw new DatabaseAccessException(message, e);
            }
        }
    }


    // Abstract methods to be implemented by extended table classes
    public abstract List<String> getColumnNames();
    public abstract List<String> getKeyColumnNames();
    protected abstract List<Object> getPrimaryKeyValues(Object obj);
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
    private String sanitizeString(String tableName) {
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
                "1)"                // Mode: REPLACE (ie, overwrite)
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
            if (database.isTest()) {
                logger.info("Attempting to restore table '{}' from backup...", tableName);
            }
            if (tableExists()) {
                clearTable();
            } else {
                createTable();
            }
            // Load data into the main table from the backup file.
            execute(getRestoreCommand(tableName, file.getAbsolutePath()));
            if (database.isTest()) {
                logger.info("table '{}' successfully restored to backup state", tableName);
            }
        } else {
            if (database.isTest()) {
                logger.error("Backup file '{}' does not exist.", file.getAbsolutePath());
            }
        }
    }


    // Initiates backup to the provided directory.
    void backup(File dir) throws IOException {
        if (tableExists()) {
            if (database.isTest()) {
                logger.info("Attempting to backup table '{}' to file...", tableName);
            }
            if (!dir.exists() && !dir.mkdirs()) {
                // Could not create the directory
                logger.error("Failed to create backup directory: {}", dir);
                throw new IOException("Unable to create backup directory.");
            }
            // Append a unique identifier to the filename for the new backup
            String tempFilename = filename.replace(
                ".sql",
                "_" + System.currentTimeMillis() + ".sql");
            execute(getBackupCommand(tableName, tempFilename));
            // Once backup is successful, delete the old backup and rename the new one
            File oldBackup = new File(filename);
            if (oldBackup.exists()) {
                oldBackup.delete();
            }
            new File(tempFilename).renameTo(oldBackup);
            if (database.isTest()) {
                logger.info("Table '{}' successfully backed up", tableName);
            }
        }
    }


    public synchronized boolean tableExists() {
        return tableExists(tableName);
    }

    /**
     * Checks whether the represented table exists in the database.
     * 
     * @return true if table exists, otherwise false.
     */
    public synchronized boolean tableExists(String tableName) {
        if (database.isTest()) {
            logger.info("Checking if table '{}' exists...", tableName);
        }
        try (
            Connection connection = database.getConnection();
            ResultSet resultSet = connection.getMetaData()
                .getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" });
        ) {
            return resultSet.next();
        } catch (SQLException e) {
            String message = "Error while checking if " + tableName + " table exists";
            logger.error(message, e);
            throw new DatabaseAccessException(message, e);
        }
    }

    /**
     * Ensures that the represented table exists in the database.
     */
    public synchronized void ensureTableExists() {
        createTable();
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
                if (database.isTest()) {
                    logger.info("Executing SQL: " + sql);
                }
                stmt.execute();
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
