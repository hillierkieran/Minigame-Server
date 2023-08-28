package minigames.server.database;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class DerbyDatabaseTests {

    private DerbyDatabase derbyDatabase = null;
    private static final String TEST_PROP_FILE_NAME = "database/DerbyDatabaseTest.properties";
    private static final Logger logger = LogManager.getLogger(DerbyDatabaseTests.class);

    @BeforeAll
    public static void setup() {
        System.setProperty("testEnv", "true");
    }

    public void InitialiseTestDatabase() {
        derbyDatabase = new DerbyDatabase(TEST_PROP_FILE_NAME);
    }

    public void buildTable() {
        if (derbyDatabase == null) {
            InitialiseTestDatabase();
        }
        // Set up the TEST_TABLE
        try (   Connection connection = derbyDatabase.getConnection();
                Statement stmt = connection.createStatement()) {
            // Drop the TEST_TABLE if it exists
            String dropTableSQL = "DROP TABLE IF EXISTS TEST_TABLE";
            stmt.execute(dropTableSQL);
            // Create the TEST_TABLE
            String createTableSQL = "CREATE TABLE TEST_TABLE(ID INT PRIMARY KEY, NAME VARCHAR(255))";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            logger.error("SQL Error encountered during operation: ", e);
        }
    }

    @AfterEach
    public void tearDown() {
        // Cleanup resources here, e.g., close database connections, clear data, etc.
        if (derbyDatabase != null) {
            derbyDatabase.shutdown();
            derbyDatabase = null;
        }
    }

    // WARNING! This test could be dangerous. Make sure you know what you're doing!
    // Deletes all test database files and directories
    @AfterAll
    public static void cleanUp() {
        // Step 1: Use a more unique and specific directory name to minimize accidental deletions
        String testDbDirectoryName = "testDb";  
        // Step 2: Check a custom system property to ensure we're in a test environment
        if (!"true".equals(System.getProperty("testEnv"))) {
            throw new IllegalStateException("Not in a test environment! Cleanup aborted.");
        }
        // Step 3: Derive the full path for the test database directory
        Path testDbPath = Paths.get(System.getProperty("user.dir"), testDbDirectoryName);
        // Step 4: Double-check that we are about to delete the correct directory
        if (!testDbPath.endsWith(testDbDirectoryName)) {
            throw new IllegalStateException("Trying to delete an unexpected directory! Cleanup aborted.");
        }
        // Step 5: Check if the directory exists before attempting deletion
        if (Files.exists(testDbPath)) {
            try {
                Files.walk(testDbPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException e) {
                logger.error("I/O Error during operation: ", e);
            }
        }
    }

    @Test // SUCESS 
    public void testTestSuiteRuns() {
        logger.info("Dummy test to show the test suite runs");
        assertTrue(true);
    }

    @Test
    public void testDefaultConstructor() {
        DerbyDatabase db = null;
        try {
            db = new DerbyDatabase();
        } catch (Exception e) {
            fail("Error initialising Derby database with default constructor");
        } finally {
            if (db != null)
            db.shutdown();
        }
    }

    @Test
    public void testAltConstructor() {
        try {
            InitialiseTestDatabase();
        } catch (Exception e) {
            fail("Error initialising Derby database with test constructor");
        }
    }

    @Test
    public void testBuildingTable() {
        try {
            buildTable();
        } catch (Exception e) {
            fail("Error building Derby database table");
        }
    }

    @Test // SUCESS 
    @DisplayName("Exception thrown when properties file missing")
    public void testMissingPropertiesFile() {
        assertThrows(RuntimeException.class, () -> new DerbyDatabase("NonexistentProperties.properties"));
    }

    @Test
    @DisplayName("Database initialises with correct schema")
    public void testDatabaseInitialisation() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        ResultSet resultSet = connection.getMetaData().getTables(null, null, "TEST_TABLE", null);
        assertTrue(resultSet.next());
        derbyDatabase.closeConnection(connection);
    }

    @Test
    @DisplayName("getConnection successfully retrieves connection")
    public void testGetConnection() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        derbyDatabase.closeConnection(connection);
    }

    @Test
    @DisplayName("closeConnection successfully closes connection")
    public void testCloseConnection() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        assertTrue(derbyDatabase.closeConnection(connection));
        assertTrue(connection.isClosed());
    }

    @Test
    @DisplayName("disconnect closes all connections")
    public void testDisconnect() {
        InitialiseTestDatabase();
        derbyDatabase.disconnect();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed());
    }

    @Test
    @DisplayName("shutdown method closes the dataSource and shuts down the Derby database")
    public void testShutdown() {
        InitialiseTestDatabase();
        derbyDatabase.shutdown();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed());
    }

    @Test
    @DisplayName("Pooling behaviour when requesting more than pool size connections")
    public void testExceedingPoolSize() {
        InitialiseTestDatabase();
        // Fetch the configured max pool size
        int configuredMaxPoolSize = ((HikariDataSource) derbyDatabase.getDataSource()).getMaximumPoolSize();
        long connectionTimeout = ((HikariDataSource) derbyDatabase.getDataSource()).getConnectionTimeout();

        // Request more connections than the configured max pool size to test the behaviour
        int numOfConnections = configuredMaxPoolSize + 2; // This will exceed the max pool by 2
        ExecutorService service = Executors.newFixedThreadPool(numOfConnections);  
        CountDownLatch latch = new CountDownLatch(numOfConnections);

        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numOfConnections; i++) {
            service.submit(() -> {
                try {
                    derbyDatabase.getConnection();
                } catch (RuntimeException e) {
                    // Capture exceptions for later assertion
                    if (e.getCause() instanceof SQLTimeoutException) {
                        exceptions.add(new TimeoutException("Connection request timed out after " + connectionTimeout + "ms."));
                    } else {
                        exceptions.add(e);
                    }
                } finally {
                    // Always count down the latch regardless of whether an exception was thrown
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        service.shutdown();

        // Assert that no exceptions were thrown during concurrent execution
        assertTrue( exceptions.isEmpty(), 
                    "Exceptions were thrown during concurrent execution: " +
                    exceptions.stream()
                        .map(Exception::getMessage)
                        .collect(Collectors.joining(", ")));
    }

    @Test
    @DisplayName("Testing concurrent connections")
    public void testConcurrentConnections() {
        InitialiseTestDatabase();
        int numOfThreads = 5;  // Sample number for concurrent threads
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        CountDownLatch latch = new CountDownLatch(numOfThreads);

        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    Connection connection = derbyDatabase.getConnection();
                    // Simulate some database work
                    Thread.sleep(100);
                    derbyDatabase.closeConnection(connection);
                } catch (InterruptedException e) {
                    // Capture exceptions for later assertion
                    exceptions.add(e);
                }
                latch.countDown();
            });
        }

        try {
            latch.await(); // Wait for all threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();

        // Assert that no exceptions were thrown during concurrent execution
    assertTrue( exceptions.isEmpty(), 
                "Exceptions were thrown during concurrent execution: " + 
                exceptions.stream()
                    .map(Exception::toString)
                    .collect(Collectors.joining(", ")));
    }

    @Test
    @DisplayName("Can write to and read from the test database using SQL")
    public void testSQLReadWrite() throws SQLException {
        buildTable();
        Connection connection = derbyDatabase.getConnection();

        // Create a test table
        String createTableSQL = "CREATE TABLE TEST_TABLE(ID INT PRIMARY KEY, NAME VARCHAR(255))";
        Statement stmt = connection.createStatement();
        stmt.execute(createTableSQL);

        // Insert data
        String insertDataSQL = "INSERT INTO TEST_TABLE VALUES (1, 'TestName')";
        stmt.execute(insertDataSQL);

        // Query data
        String queryDataSQL = "SELECT NAME FROM TEST_TABLE WHERE ID = 1";
        ResultSet rs = stmt.executeQuery(queryDataSQL);
        assertTrue(rs.next());
        assertEquals("TestName", rs.getString("NAME"));

        derbyDatabase.closeConnection(connection);
    }
}
