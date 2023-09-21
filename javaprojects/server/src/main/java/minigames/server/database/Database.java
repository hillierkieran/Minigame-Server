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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import minigames.server.utilities.Utilities;


/**
 * Abstract database class defining common operations.
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
     * Returns the default database instance based on properties file settings.
     * 
     * @return Database instance
     */
    public static Database getInstance() {
        Properties properties = Utilities.getProperties(SYS_PROP);
        String dbSystem = properties.getProperty("database.system");
        switch (dbSystem) {
            case "Derby":
                return DerbyDatabase.getInstance();
            default:
                throw new UnsupportedOperationException(
                    "Database system not supported: " + dbSystem);
        }
    }


    /**
     * Checks if a table is registered.
     * 
     * @param table The table to check.
     * @return true if registered, false otherwise.
     */
    public synchronized boolean isTableRegistered(DatabaseTable<?> table) {
        return registeredTables.contains(table);
    }


    /**
     * Registers a table for operations like backup on shutdown.
     * 
     * @param table The table to register.
     */
    public synchronized void registerTable(DatabaseTable<?> table) {
        registeredTables.add(table);
    }


    /**
     * Removes a table from the registry.
     * 
     * @param table The table to unregister.
     */
    public synchronized void unregisterTable(DatabaseTable<?> table) {
        registeredTables.remove(table);
    }


    // Remove and drop test tables; for testing purposes.
    synchronized void removeAllRegisteredTestTables() {
        registeredTables.removeIf(
            table -> table.getTableName().toLowerCase().contains("test"));
    }


    // Remove and drop all test tables; for testing purposes.
    synchronized void destroyAllRegisteredTestTables() {
        registeredTables.stream()
            .filter(table -> table.getTableName().toLowerCase().contains("test"))
            .forEach(DatabaseTable::destroyTable);
        removeAllRegisteredTestTables();
    }


    /**
     * Fetches a connection for database interaction.
     *
     * @return Database connection.
     */
    public abstract Connection getConnection();


    /**
     * Closes a connection.
     *
     * @param connection The connection to close.
     * @return true if closed successfully, false otherwise.
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
