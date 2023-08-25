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

public class DerbyDatabase implements DatabaseConnection {

    private static final Logger logger = LogManager.getLogger(DerbyDatabase.class);
    private HikariDataSource dataSource;
    private static final String PROP_FILE_NAME = "DerbyDatabase.properties";

    public DerbyDatabase() {
        connect();
    }

    private void connect() {
        try {
            initializeConnectionPool();
        } catch(Exception e) {
            logger.error("Error initializing database connection pool.", e);
            throw new RuntimeException("Failed to initialize database connection pool.", e);
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void initializeConnectionPool() throws Exception {
        HikariConfig config = new HikariConfig();
        Properties properties = loadDatabaseProperties();

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

    private Properties loadDatabaseProperties() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = DerbyDatabase.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            if (input == null) {
                throw new Exception("Unable to find " + PROP_FILE_NAME);
            }
            properties.load(input);
        }
        return properties;
    }

    private String getDatabaseLocation() throws Exception {
        URI propFileURI = DerbyDatabase.class.getClassLoader().getResource(PROP_FILE_NAME).toURI();
        String propDirPath = Paths.get(propFileURI).getParent().toString();
        return "jdbc:derby:" + propDirPath + "/derbyDatabase;create=true";
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch(SQLException e) {
            logger.error("Error fetching connection from pool.", e);
            throw new RuntimeException("Failed to fetch connection from pool.", e);
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch(SQLException e) {
                logger.error("Error closing connection.", e);
            }
        }
    }
}
