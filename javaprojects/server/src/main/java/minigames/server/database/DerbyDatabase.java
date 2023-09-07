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


/**
 * Manages database connections for the Derby Database using the HikariCP connection pool.
 * <p>
 * This class initialises a connection pool for the Derby Database, which can then be used to fetch and release
 * database connections efficiently. The configuration properties for this class are fetched from 
 * the {@code DerbyDatabase.properties} file.
 * </p>
 * <p>
 * Example implementation can be found in the {@code HighScoreAPI} class.
 * </p>
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


    // Synchronized method to control simultaneous access
    public static DerbyDatabase getInstance() {
        // First check (without locking) to improve performance
        if (instance == null || instance.isClosed()) {
            synchronized (DerbyDatabase.class) {
                // Second (double) check within the synchronized block
                if (instance == null || instance.isClosed()) {
                    instance = new DerbyDatabase();
                }
            }
        }
        return instance;
    }


    /**
     * Initializes the database connection pool upon instantiation.
     */
    private DerbyDatabase() {
        this(DEFAULT_PROP_FILE_NAME);
    }


    // for testing only
    protected DerbyDatabase(HikariDataSource mockDataSource) {
        isTest = true;
        this.dataSource = mockDataSource;
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        closed = false;
    }


    /**
     * Constructor that accepts a database properties file.
     * This is primarily designed for testing.
     * 
     * @param propFileName the properties to be used to initialise the connection pool
     */
    protected DerbyDatabase(String propFileName) {
        isTest = !propFileName.equals(DEFAULT_PROP_FILE_NAME);
        this.propFileName = propFileName;

        try {
            initialiseConnectionPool();
        } catch(Exception e) {
            logger.error("Error initialising database connection pool.", e);
            throw new RuntimeException("Failed to initialise database connection pool.", e);
        }

        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        closed = false;
    }


    // Mainly used for testing
    String getPropFileName() { return propFileName; }
    String getDefaultPropFileName() { return DEFAULT_PROP_FILE_NAME; }
    HikariDataSource getDataSource() { return dataSource; }
    public boolean isClosed() { return closed; }


    /**
     * Initialises the HikariCP connection pool with properties fetched from {@code DerbyDatabase.properties}.
     *
     * @throws IOException if properties file is not found or fails to load.
     * @throws SQLException if any error occurs during connection pool initialisation.
     */
    private void initialiseConnectionPool() throws IOException, SQLException {
        HikariConfig config = new HikariConfig();
        Properties properties = loadDatabaseProperties();

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
        //config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));

        // Close any previous dataSource
        disconnect();

        dataSource = new HikariDataSource(config);
    } /* initialiseConnectionPool */


    /**
     * Loads database properties from {@code DerbyDatabase.properties}.
     *
     * @return Properties object containing database connection details.
     * @throws IOException if properties file is not found or fails to load.
     */
    private Properties loadDatabaseProperties() throws IOException {
        Properties properties = new Properties();
        try (
            InputStream input = DerbyDatabase.class
                .getClassLoader().getResourceAsStream(propFileName)
        ) {
            if (input == null) {
                // If the properties file is not found,
                // log the error and then throw an exception
                logger.error("Unable to find {}", propFileName);

                // List all resources available to the classloader
                Enumeration<URL> resources = DerbyDatabase.class
                    .getClassLoader().getResources("");
                logger.info("Listing available resources:");
                while (resources.hasMoreElements()) {
                    logger.info("Resource: {}",
                    resources.nextElement().toString());
                }

                // Provide the path where the JVM expects to find the file
                URL expectedLocation = DerbyDatabase.class
                    .getResource(propFileName);
                logger.info(
                    "Expected location (may be null if not found): {}",
                    expectedLocation
                );

                throw new FileNotFoundException(
                    "Unable to find " + propFileName
                );
            }
            // Load properties from the input stream
            properties.load(input);
        }
        return properties;
    } /* loadDatabaseProperties */


    /**
     * Derives the database location from the location of {@code DerbyDatabase.properties} in resources directory
     *
     * @return the JDBC URL for the Derby Database.
     * @throws Exception if the URI of the properties file cannot be resolved.
     */
    private String getDatabaseLocation() throws IOException, URISyntaxException {
        try {
            // Derive the path location of the properties file
            Path propFilePath = Paths.get(DerbyDatabase.class.getClassLoader().getResource(propFileName).toURI());
            // Get the parent directory of the properties file
            Path propDirPath = propFilePath.getParent();
            // Use resolve to combine the path with the sub-directory 
            Path dbPath = propDirPath.resolve("derbyDatabase");
            // Convert path to a uniform string representation
            String dbLocation = dbPath.toAbsolutePath().toString();
            // Replace any backslashes (from Windows paths) with forward slashes to keep the JDBC URL format consistent
            dbLocation = dbLocation.replace("\\", "/");
            // Return the JDBC connection string for the Derby database
            return "jdbc:derby:" + dbLocation + ";create=true";
        } catch (NullPointerException e) {
            throw new IOException("Error fetching the properties file.", e);
        }
    }


    /**
     * Retrieves a database connection from the connection pool.
     *
     * @return A database connection.
     * @throws RuntimeException If an error occurs while fetching a connection.
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
                "Error fetching connection from pool.", e
            );
        }
    }


    /**
     * Closes the provided database connection, returning it to the pool.
     *
     * @param connection The connection to close.
     * @return {@code true} if successful, {@code false} otherwise.
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
                    "Failed to close the database connection.", e
                );
            }
        }
        return false; 
    }


    /**
     * Releases ALL database connections from the pool.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            // Close the data source, which should close all active connections.
            dataSource.close();
        }
    }


    /**
     * Gracefully shuts down the Derby database after releasing all pooled connections.
     * <p>
     * Ensures that active connections are closed and the database engine terminates 
     * properly to prevent potential resource leaks or database corruption.
     * </p>
     */
    public synchronized void shutdown() {
        if (closed) {
            return;
        }
        disconnect();
        try (
            Connection ignored = DriverManager.getConnection("jdbc:derby:;shutdown=true")
        ) {
            // Intentionally left empty
        } catch (SQLException se) {
            // Derby throws a specific SQLException when it shuts down normally.
            // We're checking for that exception here.
            if ((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState()))) {
                logger.info("Derby shut down normally");
            } else {
                logger.error("Derby did not shut down normally", se);
                throw new DatabaseAccessException(
                    "Failed to shut down the Derby database normally.", se
                );
            }
        } finally {
            closed = true;
        }
    }


    @Override
    public void close() throws SQLException {
        try {
            shutdown();
        } catch (DatabaseAccessException dae) {
            throw new SQLException(
                "Failed during Derby database shutdown.", dae
            );
        }
    }
}
