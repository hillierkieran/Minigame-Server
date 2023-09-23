package minigames.server.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import minigames.server.utilities.Utilities;


/**
 * Singleton implementation of a Derby database with HikariCP connection pooling
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabase extends Database {

    private static final String DEFAULT_PROP_FILE_NAME = "database/DerbyDatabase.properties";
    private HikariDataSource dataSource;


// Singleton Helper class

    /**
     * Holds the Singleton instance of DerbyDatabase.
     */
    private static class SingletonHelper {
        private static final DerbyDatabase INSTANCE = new DerbyDatabase(DEFAULT_PROP_FILE_NAME);
    }


// Constructors

    /**
     * Returns the Singleton instance of DerbyDatabase.
     *
     * @return DerbyDatabase instance.
     */
    public static synchronized DerbyDatabase getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Constructor initialising with a given properties file.
     *
     * @param propFileName Name of the properties file.
     */
    DerbyDatabase(String propFileName) {
        super(propFileName);
        retrieveDatabaseName();
        deleteBinaryDatabaseFiles();
        initialise();
    }

    /**
     * TESTING ONLY: Constructor for data source injection.
     *
     * @param dataSource The injected data source.
     */
    DerbyDatabase(HikariDataSource dataSource) {
        super();
        this.dataSource = dataSource;
        isTest = true;
        closed = false;
    }


// Getters

    public boolean isDisconnected() { return dataSource == null || dataSource.isClosed(); }
    public boolean isReady() { return !isClosed() && !isDisconnected(); }
    String getDefaultPropFileName() { return DEFAULT_PROP_FILE_NAME; }
    HikariDataSource getDataSource() { return dataSource; }


// Core functions

    /**
     * Gets a connection to the database from the connection pool.
     *
     * @return an active Connection to the database.
     * @throws DatabaseAccessException if unable to get connection.
     */
    @Override
    public Connection getConnection() {
        try {
            if (!isReady()) initialise(); // Try to revive if dead
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new DatabaseAccessException("Failed to obtain a connection.", e);
        }
    }

    /**
     * Closes provided connection.
     *
     * @param connection The connection to be closed.
     * @return true if successfully closed, false otherwise.
     * @throws DatabaseAccessException if an error occurs.
     */
    @Override
    public boolean closeConnection(Connection connection) throws DatabaseAccessException {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    return true;
                }
            } catch (SQLException e) {
                throw new DatabaseAccessException("Error closing connection.", e);
            }
        }
        return false;
    }

    /**
     * Closes the Derby database.
     *
     * @throws SQLException on error.
     */
    @Override
    public synchronized void close() throws SQLException {
        if (!closed) {
            try {
                shutdown();
            } catch (DatabaseException e) {
                Throwable cause = e.getCause();
                if (cause instanceof SQLException) {
                    throw (SQLException) cause;
                } else {
                    throw new SQLException(cause);
                }
            }
        }
    }


// Initialisation functions

    /**
     * (Re)Initialises the database.
     */
    public void initialise() {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            initialiseConnectionPool();
        } catch(SQLException e) {
            throw new RuntimeException("Failed to initialise Derby database" +
                (databaseName != null ? " '" + databaseName + "'" : "") + ".", e);
        }
        closed = false;
    }

    /**
     * Retrieve the database's name using a properties file.
     */
    private void retrieveDatabaseName(){
        Properties properties = Utilities.getProperties(propFileName);
        databaseName = properties.getProperty("db.jdbcUrl").split(":")[2].split(";")[0];
    }

    /**
     * Initialises the connection pool using a properties file.
     *
     * @throws SQLException on error.
     */
    private void initialiseConnectionPool() throws SQLException {
        Properties properties = Utilities.getProperties(propFileName);
        HikariConfig config = new HikariConfig();
        // Set connection pool parameters from properties file
        config.setJdbcUrl(properties.getProperty("db.jdbcUrl"));
        config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));
        disconnect(); // Close any previous dataSource
        dataSource = new HikariDataSource(config);
        closed = false;
    }

    /**
     * Registers a shutdown hook for the database.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdownDerbySystem();
                } catch (DatabaseShutdownException e) {
                    logger.error("Error during database shutdown.", e);
                }
            }
        });
    }


// Shutdown functions

    /**
     * Shuts down the database, disconnects pool and backs up tables.
     *
     * @throws DatabaseShutdownException on error.
     */
    public synchronized void shutdown() throws DatabaseShutdownException {
        if (closed) return; // Check if already closed
        // Backup all tables
        for (DatabaseTable<?> table : registeredTables) {
            try {
                table.backup();
            } catch (DatabaseException e) {
                logger.error(
                    "Failed to backup database table " + table.getTableName(), e);
            }
        }
        disconnect(); // Releasing connection pool
        if (databaseName == null && isTest) {
            closed = true; return; // If mock test, skip shutdown calls
        }
        try {
            DriverManager.getConnection( // shutdown specific database
                "jdbc:derby:" + databaseName + ";shutdown=true;deregister=false");
        } catch (SQLException se) {
            if (!se.getSQLState().equals("08006")) {
                throw new DatabaseShutdownException(
                    "The Derby database failed to shut down gracefully.", se);
            }
        } finally {
            closed = true;
        }
    }

    /**
     * Disconnects the connection pool.
     */
    protected synchronized void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Shuts down the whole Derby system completely.
     *
     * @throws DatabaseShutdownException on error.
     */
    public synchronized void shutdownDerbySystem() throws DatabaseShutdownException {
        shutdown(); // Contains instance-specific resource shutdown handling
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true"); // whole Derby system
        } catch (SQLException se) {
            if (!se.getSQLState().equals("XJ015")) {
                throw new DatabaseShutdownException(
                    "The Derby system failed to shut down gracefully.", se);
            }
        }
    }


// File deletion functions
// WARNING! Editing the following logic could be dangerous.

    // Remove binary database files and directories
    private void deleteBinaryDatabaseFiles() {
        if (databaseName == null) return;
        int retries = 10;
        // Derive the full path for the database directory
        Path databasePath = Paths.get(System.getProperty("user.dir"), databaseName);
        // Double-check that we are about to delete the correct directory
        if (!databasePath.endsWith(databaseName)) {
            logger.error("Trying to delete an unexpected directory! Cleanup aborted.");
            return;
        }
        // Check if the directory exists from a previous and attempt deletion
        if (Files.exists(databasePath)) {
            logger.info("Found database directory: " + databasePath + ". Preparing to delete.");
            try {
                // Delete all nested files and directories in reverse order
                // to ensure that directories are empty before they get deleted
                Files.walk(databasePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        logger.debug("Deleting file or directory: " + file.getPath());
                        file.delete();
                    });
                // Add a check to make sure the directory is indeed deleted
                while(retries > 0 && Files.exists(databasePath)) {
                    Thread.sleep(1000); // wait for 500ms
                    retries--;
                }
                if(Files.exists(databasePath)) {
                    logger.error("Unable to delete the database directory after multiple retries.");
                }
            } catch (IOException e) {
                logger.error("I/O Error during deletion: ", e);
            } catch (InterruptedException ie) {
                logger.error("Interrupted during wait: ", ie);
                Thread.currentThread().interrupt(); // Handle the interrupt
            }
        }
    }
}
