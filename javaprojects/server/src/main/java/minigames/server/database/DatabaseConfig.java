package minigames.server.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Manages database configuration, allowing for easy database system switching.
 * Configuration is based on the `config.properties` file.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DatabaseConfig {

    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);

    private static Properties properties;


    // Load the properties file on class initialization
    static {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
            .getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logger.error(e);
        }
    }


    /**
     * Fetches the default database instance based on the system specified in the properties file.
     * 
     * @return Database instance of the system specified in the configuration.
     * @throws UnsupportedOperationException if the specified database system is not supported.
     */
    public static Database getDefaultInstance() {
        String dbSystem = properties.getProperty("database.system");
        switch (dbSystem) {
            case "Derby":
                return DerbyDatabase.getInstance();
            // TODO: Add more cases as more database systems are added
            default:
                throw new UnsupportedOperationException(
                    "Database system not supported: " + dbSystem
                );
        }
    }
}
