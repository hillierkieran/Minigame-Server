package minigames.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DerbyDatabaseAPI {

    private HikariDataSource dataSource;
    private static final String PROP_FILE_NAME = "DerbyDatabase.properties";

    public DerbyDatabaseAPI() throws SQLException, URISyntaxException, IOException {

        HikariConfig config = new HikariConfig();

        // Load properties from `database.properties` file in resources dir
        Properties properties = new Properties();
        try (InputStream input = DerbyDatabaseAPI.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROP_FILE_NAME);
            }
            properties.load(input);
        }

        // Get location of database data
        URI propFileURI = DerbyDatabaseAPI.class.getClassLoader().getResource(PROP_FILE_NAME).toURI();
        File propFile = new File(propFileURI);
        String propDirPath = propFile.getParent();
        String dbURL = "jdbc:derby:" + propDirPath + "/derbyDatabase;create=true";

        // HikariCP settings
        config.setJdbcUrl(dbURL);
        config.setDriverClassName(properties.getProperty("db.driverClass"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maxPoolSize")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minIdle")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout")));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("hikari.validationTimeout")));
        config.setConnectionTestQuery(properties.getProperty("hikari.connectionTestQuery"));

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() {
        return dataSource.getConnection();
    }

    public void closeConnection() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
