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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import minigames.server.database.Database;
import minigames.server.database.DatabaseAccessException;
import minigames.server.database.DatabaseInitialisationException;
import minigames.server.database.DatabaseShutdownException;


/**
 * This class provides a Singleton implementation of a Derby database using HikariCP for connection pooling.
 * The database is initialized and shut down gracefully when the application is started or closed.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabase implements Database {

    private static final Logger logger = LogManager.getLogger(DerbyDatabase.class);
    private static final String DEFAULT_PROP_FILE_NAME = "database/DerbyDatabase.properties";

    private static volatile DerbyDatabase instance;
    private volatile boolean closed = true;

    private HikariDataSource dataSource;
    private String propFileName; 
    private boolean isTest = false;


    /**
     * Retrieves the Singleton instance of DerbyDatabase. 
     * If the database has not been instantiated or has been closed,
     * it initialises a new instance.
     *
     * @return the single instance of DerbyDatabase.
     */
    public static DerbyDatabase getInstance() {
        // First check (without locking)
        if (instance == null || instance.isClosed()) {
            synchronized (DerbyDatabase.class) {
                // Second (double) check within the synchronized block
                // ie. only let one thread in this block at a time
                if (instance == null || instance.isClosed()) {
                    instance = new DerbyDatabase(DEFAULT_PROP_FILE_NAME);
                }
            }
        }
        return instance;
    }


    /**
     * Constructor that accepts a database properties file.
     * 
     * @param propFileName the properties to be used to initialise the connection pool
     */
    protected DerbyDatabase(String propFileName) {
        // Determine if this is a test
        isTest = !propFileName.equals(DEFAULT_PROP_FILE_NAME);
        this.propFileName = propFileName;
        // Setup database
        try {
            initialiseConnectionPool();
        } catch(Exception e) {
            logger.error("Error initialising database connection pool.", e);
            throw new RuntimeException("Failed to initialise database connection pool.", e);
        }
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        // Indicate that database is running
        closed = false;
    }


    // Constructor used for testing purposes, allowing for a mock data source.
    protected DerbyDatabase(HikariDataSource mockDataSource) {
        isTest = true;
        this.dataSource = mockDataSource;
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        closed = false;
    }


    // Getters mainly used for testing
    String getPropFileName() { return propFileName; }
    String getDefaultPropFileName() { return DEFAULT_PROP_FILE_NAME; }
    HikariDataSource getDataSource() { return dataSource; }

    // Get state of database
    public boolean isClosed() { return closed; }


    // Load the embedded Derby JDBC driver.
    private void loadDerbyEmbeddedDriver() {
        try {
            // Ensure the Derby embedded driver is loaded
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (ClassNotFoundException e) {
            // Handle the ClassNotFoundException
            logger.error("Derby JDBC EmbeddedDriver class not found", e);
            throw new DatabaseInitialisationException("Derby JDBC EmbeddedDriver class not found.", e);
        } catch (InstantiationException e) {
            // Handle the InstantiationException
            logger.error("Failed to instantiate Derby JDBC EmbeddedDriver", e);
            throw new DatabaseInitialisationException("Failed to instantiate Derby JDBC EmbeddedDriver.", e);
        } catch (IllegalAccessException e) {
            // Handle the IllegalAccessException
            logger.error("Illegal access while trying to instantiate Derby JDBC EmbeddedDriver", e);
            throw new DatabaseInitialisationException("Illegal access while trying to instantiate Derby JDBC EmbeddedDriver.", e);
        }
    }


    /**
     * Initialises the connection pool by loading the embedded Derby driver, 
     * setting HikariCP configuration, and establishing the pool.
     */
    private void initialiseConnectionPool() throws IOException, SQLException {
        // Load the embedded Derby JDBC driver.
        try {
            loadDerbyEmbeddedDriver();
        } catch (DatabaseInitialisationException e) {
            logger.error("Failed to load Derby EmbeddedDriver", e);
            throw new DatabaseInitialisationException("Failed to load Derby EmbeddedDriver", e);
        }
        HikariConfig config = new HikariConfig();
        Properties properties = loadDatabaseProperties();
        // Determine where to locate/setup the database
        try {
            if (isTest) {
                config.setJdbcUrl(properties.getProperty("db.jdbcUrl"));
            } else {
                config.setJdbcUrl(getDatabaseLocation());
            }
        } catch(URISyntaxException e) {
            logger.error("Error occurred while fetching the database location.", e);
            throw new IOException("Failed to determine database location.", e);
        }
        // Set connection pool parameters from properties file
        config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        //config.setDriverClassName(properties.getProperty("db.driverClass"));
        logger.debug(config.getDataSourceClassName());
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
    } /* initialiseConnectionPool */


    // Loads database properties from properties file
    private Properties loadDatabaseProperties() throws IOException {
        // Try to fetch properties file
        Properties properties = new Properties();
        try (
            InputStream input = DerbyDatabase.class
                .getClassLoader().getResourceAsStream(propFileName)
        ) {
            // Check if found
            if (input == null) {
                // Not found. Log the error and throw an exception
                logger.error("Unable to find {}", propFileName);
                throw new DatabaseInitialisationException(
                    "Failed to locate " + propFileName +
                    " file for database configuration."
                );
            }
            // Found. Load properties from the input stream
            properties.load(input);
        }
        return properties;
    } /* loadDatabaseProperties */



    // Derive the database location by leveraging the location of properties file
    private String getDatabaseLocation() throws IOException, URISyntaxException {
        try {
            // Derive and copy the path location of the properties file
            Path propFilePath = Paths.get(DerbyDatabase.class.getClassLoader().getResource(propFileName).toURI());
            Path propDirPath = propFilePath.getParent();
            Path dbPath = propDirPath.resolve("derbyDatabase");
            String dbLocation = dbPath.toAbsolutePath().toString();
            // Replace any Windows backslashes to keep the JDBC URL format consistent
            dbLocation = dbLocation.replace("\\", "/");
            // Return the JDBC connection string for the Derby database
            return "jdbc:derby:" + dbLocation + ";create=true";
        } catch (NullPointerException e) {
            // Failed to find the properties file
            throw new DatabaseInitialisationException(
                "Unable to determine the location of the database " +
                "based on the properties file location.", e
            );
        }
    }


    /**
     * Establishes a connection to the database using the connection pool.
     * If the connection fails, it attempts to close the connection
     * and throws a DatabaseAccessException.
     *
     * @return an active Connection to the database.
     * @throws DatabaseAccessException if there's an error fetching the connection.
     */
    @Override
    public Connection getConnection() {
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
                    connection.close();
                } catch(SQLException closeException) {
                    logger.error("Error closing failed connection.", closeException);
                }
            }
            logger.error("Error fetching connection from pool.", e);
            throw new DatabaseAccessException(
                "Failed to obtain a connection from the connection pool. " +
                "The database might be unavailable or overburdened.", e
            );
        }
    }


    /**
     * Closes the provided database connection, returning it to the pool.
     *
     * @param connection The connection to be closed.
     * @return true if the connection was closed successfully, false otherwise.
     * @throws DatabaseAccessException if there's an error closing the connection.
     */
    @Override
    public boolean closeConnection(Connection connection) {
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


    /**
     * Disconnects the data source by closing the connection pool,
     * releasing all connections.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            // Close the data source, which should close all active connections.
            dataSource.close();
        }
    }


    /**
     * Shuts down the Derby database and ensures all resources are released.
     * 
     * @throws DatabaseShutdownException if there's an error during the shutdown process.
     */
    public synchronized void shutdown() {
        if (closed) {
            return;
        }
        disconnect();
        try (
            Connection ignored = DriverManager.getConnection("jdbc:derby:;shutdown=true;deregister=false")
        ) {
            // Intentionally left empty
        } catch (SQLException se) {
            // Derby throws a specific SQLException when it shuts down normally.
            // We're checking for that exception here.
            if ((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState()))) {
                logger.info("Derby shut down normally");
            } else {
                logger.error("Derby did not shut down normally", se);
                throw new DatabaseShutdownException(
                    "The Derby database failed to shut down gracefully. " +
                    "This might lead to potential data corruption or resource leaks.", se
                );
            }
        } finally {
            closed = true;
        }
    }


    /**
     * Closes the Derby database, an alias for the shutdown method.
     * 
     * @throws SQLException if there's an error during the shutdown process.
     */
    @Override
    public void close() throws SQLException {
        try {
            shutdown();
        } catch (DatabaseShutdownException dae) {
            throw new SQLException(
                "Failed during Derby database shutdown.", dae
            );
        }
    }
}
