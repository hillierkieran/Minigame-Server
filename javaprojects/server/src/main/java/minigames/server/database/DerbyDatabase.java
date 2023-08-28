package minigames.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class DerbyDatabase implements DatabaseConnection {

    private static final Logger logger = LogManager.getLogger(DerbyDatabase.class);
    private static final String DEFAULT_PROP_FILE_NAME = "database/DerbyDatabase.properties";

    private HikariDataSource dataSource;
    private String propFileName; 
    private boolean isTest = false; 

    /**
     * Initializes the database connection pool upon instantiation.
     */
    public DerbyDatabase() {
        this(DEFAULT_PROP_FILE_NAME);
    }

    /**
     * Constructor that accepts a database properties file.
     * This is primarily designed for testing.
     * 
     * @param propFileName the properties to be used to initialise the connection pool
     */
    public DerbyDatabase(String propFileName) {
        isTest = propFileName != DEFAULT_PROP_FILE_NAME;
        this.propFileName = propFileName;

        try {
            initialiseConnectionPool();
        } catch(Exception e) {
            logger.error("Error initialising database connection pool.", e);
            throw new RuntimeException("Failed to initialise database connection pool.", e);
        }
    }

    // Used mostly for testing
    public HikariDataSource getDataSource() { return dataSource; }

    /**
     * Initialises the HikariCP connection pool with properties fetched from {@code DerbyDatabase.properties}.
     *
     * @throws IOException if properties file is not found or fails to load.
     * @throws SQLException if any error occurs during connection pool initialisation.
     */
    void initialiseConnectionPool() throws IOException, SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            logger.error("Unable to load Derby JDBC driver.", e);
            throw new RuntimeException("Failed to load Derby JDBC driver.", e);
        }
        // Create a HikariCP configuration object
        HikariConfig config = new HikariConfig();
        // Load properties from the configuration file
        Properties properties = loadDatabaseProperties();

        try {
            // Populate the HikariCP configuration object with database properties
            if (isTest) {
                config.setJdbcUrl(properties.getProperty("db.jdbcUrl"));
            } else {
                config.setJdbcUrl(getDatabaseLocation());
            }
        } catch(URISyntaxException e) {
            logger.error("Error occurred while fetching the database location.", e);
            throw new IOException("Failed to determine database location.", e);
        }
        config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));

        // Close any previous dataSource
        disconnect();

        dataSource = new HikariDataSource(config);
    }

    /**
     * Loads database properties from {@code DerbyDatabase.properties}.
     *
     * @return Properties object containing database connection details.
     * @throws IOException if properties file is not found or fails to load.
     */
    private Properties loadDatabaseProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DerbyDatabase.class.getClassLoader().getResourceAsStream(propFileName)) {
            if (input == null) {
                // If the properties file is not found, log the error and then throw an exception
                logger.error("Unable to find {}", propFileName);

                // List all resources available to the classloader (this might be a long list)
                Enumeration<URL> resources = DerbyDatabase.class.getClassLoader().getResources("");
                logger.info("Listing available resources:");
                while (resources.hasMoreElements()) {
                    logger.info("Resource: {}", resources.nextElement().toString());
                }

                // Provide the path where the JVM expects to find the file
                URL expectedLocation = DerbyDatabase.class.getResource(propFileName);
                logger.info("Expected location (may be null if not found): {}", expectedLocation);

                throw new FileNotFoundException("Unable to find " + propFileName);
            }
            // Load properties from the input stream
            properties.load(input);
        }
        return properties;
    }

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
            throw new RuntimeException("Failed to fetch connection from pool.", e);
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
                return false;
            }
        }
        return false; 
    }

    /**
     * Releases ALL database connections from the pool.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
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
    public void shutdown() {
        disconnect();  // Ensure all pooled connections are released
        try (Connection ignored = DriverManager.getConnection("jdbc:derby:;shutdown=true")) {
            // The above connection attempt will always throw an exception because 
            // Derby shuts down and doesn't return a valid connection.
        } catch (SQLException se) {
            if ((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState()))) {
                // We got the expected exception.
                logger.info("Derby shut down normally");
            } else {
                logger.error("Derby did not shut down normally", se);
            }
        }
    }
}
