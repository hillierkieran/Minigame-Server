package minigames.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
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
    private HikariDataSource dataSource;
    private static final String PROP_FILE_NAME = "DerbyDatabase.properties";

    /**
     * Constructor that connects to the Derby Database by initialising the connection pool.
     */
    public DerbyDatabase() {
        connect();
    }

    /**
     * Establishes the connection by initialising the connection pool.
     */
    private void connect() {
        try {
            initialiseConnectionPool();
        } catch(Exception e) {
            logger.error("Error initialising database connection pool.", e);
            throw new RuntimeException("Failed to initialise database connection pool.", e);
        }
    }

    /**
     * Closes the connection pool.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Initialises the HikariCP connection pool with properties fetched from {@code DerbyDatabase.properties}.
     *
     * @throws Exception if any error occurs during connection pool initialisation.
     */
    private void initialiseConnectionPool() throws Exception {
        // Create a HikariCP configuration object
        HikariConfig config = new HikariConfig();
        // Load properties from the configuration file
        Properties properties = loadDatabaseProperties();

        // Populate the HikariCP configuration object with database properties
        config.setJdbcUrl(getDatabaseLocation());
        config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));

        dataSource = new HikariDataSource(config);
    }

    /**
     * Loads database properties from {@code DerbyDatabase.properties}.
     *
     * @return Properties object containing database connection details.
     * @throws Exception if property file is not found or fails to load.
     */
    private Properties loadDatabaseProperties() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = DerbyDatabase.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            if (input == null) {
                // If the properties file is not found, throw an exception
                throw new Exception("Unable to find " + PROP_FILE_NAME);
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
    private String getDatabaseLocation() throws Exception {
        // Derive the URI location of the properties file
        URI propFileURI = DerbyDatabase.class.getClassLoader().getResource(PROP_FILE_NAME).toURI();
        // Construct the directory path of the properties file
        String propDirPath = Paths.get(propFileURI).getParent().toString();
        // Return the JDBC connection string for the Derby database
        return "jdbc:derby:" + propDirPath + "/derbyDatabase;create=true";
    }

    /**
     * Fetches a connection from the connection pool.
     *
     * @return a {@code Connection} object.
     * @throws RuntimeException if fetching connection from the pool fails.
     */
    @Override
    public Connection getConnection() {
        try {
            // Fetch a connection from the HikariCP data source
            return dataSource.getConnection();
        } catch(SQLException e) {
            // If there's an error, log it and propagate it as a runtime exception
            logger.error("Error fetching connection from pool.", e);
            throw new RuntimeException("Failed to fetch connection from pool.", e);
        }
    }

    /**
     * Closes the provided database connection, returning it to the pool.
     *
     * @param connection the database connection to be closed.
     */
    @Override
    public void closeConnection(Connection connection) {
        if(connection != null) {
            try {
                // Return the connection to the connection pool
                connection.close();
            } catch(SQLException e) {
                // If there's an error during connection closure, log it
                logger.error("Error closing connection.", e);
            }
        }
    }
}
