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
 * Represents the main abstract class for a database, defining common database operations.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public abstract class Database implements AutoCloseable{

    private static final String TEST_ENV = "testEnv";
    private static final String SYS_PROP = "config.properties";
    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected volatile boolean closed = true;
    protected volatile boolean isTest = false;

    protected String propFileName;
    protected String databaseName;
    protected final List<DatabaseTable<?>> registeredTables = new CopyOnWriteArrayList<>();

    protected Database() {
        this(null);
    }

    protected Database(String propFileName) {
        this.propFileName = propFileName;
        isTest = "true".equals(System.getProperty(TEST_ENV));
    }


    // Getters
    public String getPropFileName() { return propFileName; }
    public boolean isClosed() { return closed; }
    public boolean isTest() { return isTest; }
    public List<DatabaseTable<?>> getRegisteredTables() {
        return new ArrayList<>(registeredTables); // return a copy
    }


    /**
     * Fetches the default database instance based on the system specified in the properties file.
     * 
     * @return Database instance of the system specified in the configuration.
     * @throws UnsupportedOperationException if the specified database system is not supported.
     */
    public static Database getInstance() {
        Properties properties = Utilities.getProperties(SYS_PROP);
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


    /**
     * Check if a table is in table registry.
     * 
     * @param table The table to check.
     */
    public synchronized boolean isTableRegistered(DatabaseTable<?> table) {
        return registeredTables.contains(table);
    }

    /**
     * Register a table for backup on shutdown, tracking and batch operations.
     * 
     * @param table The table to register.
     */
    public synchronized void registerTable(DatabaseTable<?> table) {
        registeredTables.add(table);
    }

    /**
     * Unregister (remove) a table from table registry.
     * 
     * @param table The table to unregister.
     */
    public synchronized void unregisterTable(DatabaseTable<?> table) {
        if (registeredTables.contains(table)) {
            registeredTables.remove(table);
        }
    }

    // TESTING ONLY - Remove all test tables from the registry
    synchronized void removeAllRegisteredTestTables() {
        if (isTest) {
            for (DatabaseTable<?> table : registeredTables) {
                if (table.getTableName().toLowerCase().contains("test")) {
                    registeredTables.remove(table);
                }
            }
        }
    }

    // TESTING ONLY - Drop all test tables from the registry
    synchronized void destroyAllRegisteredTestTables() {
        if (isTest) {
            for (DatabaseTable<?> table : registeredTables) {
                if (table.getTableName().toLowerCase().contains("test")) {
                    table.destroyTable();
                }
            }
        }
    }


    /**
     * Gets a connection from the connection pool or the database directly.
     *
     * @return a {@link Connection} object for database interaction.
     */
    public abstract Connection getConnection();


    /**
     * Closes the provided connection, returning it back to the connection pool
     * or releasing the resource.
     *
     * @param connection The {@link Connection} to be closed.
     * @return true if the connection is successfully closed, false otherwise.
     */
    public abstract boolean closeConnection(Connection connection);


    /**
     * Closes the database
     * 
     * @throws SQLException if there's an error during the shutdown process.
     */
    @Override
    public abstract void close() throws SQLException;
}
