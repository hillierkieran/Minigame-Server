package minigames.server.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import minigames.server.utilities.Utilities;


/**
 * This class provides a Singleton implementation of a Derby database using HikariCP for connection pooling.
 * The database is initialized and shut down gracefully when the application is started or closed.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabase extends Database {

    private static final String DEFAULT_PROP_FILE_NAME = "database/DerbyDatabase.properties";

    private HikariDataSource dataSource;


    private static class SingletonHelper {
        private static final DerbyDatabase INSTANCE = new DerbyDatabase(DEFAULT_PROP_FILE_NAME);
    }


    /**
     * Retrieves the Singleton instance of DerbyDatabase.
     *
     * @return the single instance of DerbyDatabase.
     */
    public static synchronized DerbyDatabase getInstance() {
        return SingletonHelper.INSTANCE;
    }


    // Constructor that accepts properties file insertion.
    DerbyDatabase(String propFileName) {
        super(propFileName);
        try {
            RegisterDerbyEmbeddedDriver();
            initialiseConnectionPool();
        } catch(SQLException e) {
            String message = "Failed to initialise Derby database" + 
                (databaseName != null ? ": " + databaseName : "") + ".";
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
        registerShutdownHook();
        closed = false;
    }


    // TESTING ONLY - Constructor allowing for data source injection.
    DerbyDatabase(HikariDataSource dataSource) {
        super();
        this.dataSource = dataSource;
        isTest = true;
        closed = false;
    }


    // Get state of database
    public boolean isDisconnected() { return dataSource != null && dataSource.isClosed(); }
    public boolean isReady() { return !isDisconnected() && !isClosed(); }


    // TESTING ONLY - getters
    String getDefaultPropFileName() { return DEFAULT_PROP_FILE_NAME; }
    HikariDataSource getDataSource() { return dataSource; }


    // Register the embedded Derby JDBC driver.
    private void RegisterDerbyEmbeddedDriver() throws SQLException {
        try {
            // Ensure the Derby embedded driver is loaded and registered
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        } catch (SQLException e) {
            // Handle the SQLException that might arise from DriverManager
            logger.error("Failed to register Derby JDBC EmbeddedDriver", e);
            throw e;
        }
    }


    // Initialise the connection pool
    private void initialiseConnectionPool() throws SQLException {
        HikariConfig config = new HikariConfig();
        Properties properties = Utilities.getProperties(propFileName);
        // Set connection pool parameters from properties file
        config.setJdbcUrl(properties.getProperty("db.jdbcUrl"));
        databaseName = properties.getProperty("db.jdbcUrl").split(":")[2].split(";")[0];
        config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));
        // Close any previous dataSource
        disconnect();
        // Initialise the connection pool
        dataSource = new HikariDataSource(config);
        closed = false;
    }


    // Register shutdown hook
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


    /**
     * Get a connection to the database from the connection pool.
     *
     * @return an active Connection to the database.
     * @throws DatabaseAccessException if there's an error fetching the connection.
     */
    @Override
    public Connection getConnection() throws DatabaseAccessException {
        if (isDisconnected()) {
            try {
                initialiseConnectionPool();
            } catch (Exception e) {
                String message = "Failed re-initialising database connection pool.";
                logger.error(message, e);
                throw new DatabaseAccessException(message, e);
            }
        }
        Connection connection = null;
        try {
            // Fetch a connection from the HikariCP data source
            connection = dataSource.getConnection();
            return connection;
        } catch(SQLException e) {
            // If there's an error, close the connection, log it, 
            // and propagate it as a runtime exception
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch(SQLException closeException) {
                    logger.error("Error closing failed connection.", closeException);
                }
            }
            String message = "Failed to obtain a connection from the connection pool.";
            logger.error(message, e);
            throw new DatabaseAccessException(message, e);
        }
    }


    /**
     * Closes a database connection, returning it to the connection pool.
     *
     * @param connection The connection to be closed.
     * @return true if the connection was closed successfully, false otherwise.
     * @throws DatabaseAccessException if there's an error closing the connection.
     */
    @Override
    public boolean closeConnection(Connection connection) throws DatabaseAccessException {
        if(connection != null) {
            try {
                // Return the connection to the connection pool
                connection.close();
                return true;
            } catch(SQLException e) {
                // If there's an error during connection closure, log it
                logger.error("Error closing connection.", e);
                throw new DatabaseAccessException(
                    "Failed to return the connection back to the pool. " +
                    "The connection might have already been closed " +
                    "or become unresponsive.", e
                );
            }
        }
        return false; 
    }


    // Disconnect connection pool, closing and releasing all connections.
    protected synchronized void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }


    /**
     * Shutdown this database, disconnect connection pool and backup tables to file.
     * 
     * @throws DatabaseShutdownException if there's an error during the shutdown process.
     */
    public synchronized void shutdown() throws DatabaseShutdownException {
        // Check if already closed
        if (closed) {
            return;
        }
        // Backup all table data to respective files
        if (!isTest) {
            for (DatabaseTable<?> table : registeredTables) {
                try {
                    table.backup();
                } catch (IOException e) {
                    logger.error(
                        "Failed to backup database table " + table.getTableName(), e);
                }
            }
        }
        disconnect(); // Releasing connection pool
        if (databaseName == null && isTest) {
            closed = true;
            return; // If mock test, skip shutdown calls
        }
        try {
            // Call shutdown command for specific database
            DriverManager.getConnection(
                "jdbc:derby:" + databaseName + ";shutdown=true;deregister=false");
        } catch (SQLException se) {
            if ( // Check for the specific SQLExceptions that indicate success.
                se.getSQLState().equals("08006") &&
                se.getErrorCode() == 45000
            ) {
                logger.info(databaseName + " was shut down successfully.");
            } else {
                String message = "The Derby database failed to shut down gracefully.";
                logger.error(message, se);
                throw new DatabaseShutdownException(message, se);
            }
        } finally {
            closed = true;
        }
    }


    public synchronized void shutdownDerbySystem() throws DatabaseShutdownException {
        shutdown(); // Contains instance-specific resource shutdown handling 
        try {
            // Call shutdown command for whole Derby system
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException se) {
            if ( // Check for the specific SQLExceptions that indicate success.
                se.getSQLState().equals("XJ015") &&
                se.getErrorCode() == 50000
            ) {
                logger.info("Derby system was shut down successfully");
            } else {
                String message = "The Derby system failed to shut down gracefully.";
                logger.error(message, se);
                throw new DatabaseShutdownException(message, se);
            }
        }
    }


    /**
     * Closes the Derby database, an alias for the shutdown method.
     * 
     * @throws SQLException if there's an error during the shutdown process.
     */
    @Override
    public synchronized void close() throws SQLException {
        if (!closed) {
            try {
                shutdown();
            } catch (DatabaseShutdownException dae) {
                throw new SQLException(
                    "Failed during Derby database shutdown.", dae
                );
            }
        }
    }
}
