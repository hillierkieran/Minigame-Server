package minigames.server.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is responsible for conducting tests on the DerbyDatabase class.
 * All tests are conducted using JUnit and ensure the proper functioning of database operations.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabaseTests {

    private DerbyDatabase derbyDatabase = null;
    private static final String TEST_PROP_FILE_NAME = "database/DerbyDatabaseTest.properties";
    private static final Logger logger = LogManager.getLogger(DerbyDatabaseTests.class);


    /**
     * Set-up configurations that apply for all test cases. 
     * This specific setup indicates a test environment.
     */
    @BeforeAll
    public static void setup() {
        System.setProperty("testEnv", "true");
    }


    /**
     * Re-initialises the test database before each test.
     */
    @BeforeEach
    public void InitialiseTestDatabase() {
        tearDown();
        derbyDatabase = new DerbyDatabase(TEST_PROP_FILE_NAME);
    }


    /**
     * Builds a sample table 'TEST_TABLE' in the database.
     */
    public void buildTable() {
        InitialiseTestDatabase();
        // Set up the TEST_TABLE
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement()
        ) {
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


    /**
     * Cleans up resources after each test, such as closing database connections and clearing data.
     */
    @AfterEach
    public void tearDown() {
        // Cleanup resources here, e.g., close database connections, clear data, etc.
        if (derbyDatabase != null) {
            derbyDatabase.shutdown();
            derbyDatabase = null;
        }
    }


    /**
     * WARNING! Editing this method could be dangerous. 
     * This method removes all test database files and directories after all tests are complete.
     */
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
    } /* cleanUp */


    /**
     * This is a dummy test to make sure the test suite runs.
     */
    @Test
    @DisplayName("Ensure the test suite runs correctly")
    public void testTestSuiteRuns() {
        logger.info("Dummy test to show the test suite runs");
        assertTrue(true);
    }


    /**
     * Testing default constructor for the database.
     */
    @Test
    @DisplayName("Default constructor initializes without errors")
    public void testDefaultConstructor() {
        try (DerbyDatabase db = new DerbyDatabase()) {
            assertNotNull(db);
        } catch (Exception e) {
            fail("Error initialising Derby database with default constructor", e);
        }
    }


    /**
     * Testing test-constructor for the database.
     */
    @Test
    @DisplayName("Test-constructor initializes without errors using test properties")
    public void testAltConstructor() {
        try {
            InitialiseTestDatabase();
        } catch (Exception e) {
            fail("Error initialising Derby database with test constructor");
        }
    }


    /**
     * Test for building the sample TEST_TABLE.
     */
    @DisplayName("Building the TEST_TABLE successfully in the database")
    @Test
    public void testBuildingTable() {
        try {
            buildTable();
        } catch (Exception e) {
            fail("Error building Derby database table");
        }
    }


    /**
     * Tests the scenario when a properties file is missing.
     */
    @Test
    @DisplayName("Exception is thrown when the properties file is missing")
    public void testMissingPropertiesFile() {
        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> new DerbyDatabase("NonexistentProperties.properties"));
        assertTrue(thrown.getCause() instanceof FileNotFoundException);
        assertTrue(thrown.getCause().getMessage().contains("Unable to find"));
    }


    /**
     * Tests the database initialisation with the correct schema.
     */
    @Test 
    @DisplayName("Database initializes with the expected TEST_TABLE schema")
    public void testDatabaseInitialisation() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        ResultSet resultSet = connection.getMetaData().getTables(null, null, "TEST_TABLE", null);
        assertTrue(resultSet.next());
        derbyDatabase.closeConnection(connection);
    }


    /**
     * Ensures the database connection can be successfully fetched.
     */
    @Test
    @DisplayName("Database connection can be successfully retrieved")
    public void testGetConnection() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        derbyDatabase.closeConnection(connection);
    }


    /**
     * Ensures the database connection can be successfully closed.
     */
    @Test
    @DisplayName("Database connection can be successfully closed")
    public void testCloseConnection() throws SQLException {
        InitialiseTestDatabase();
        Connection connection = derbyDatabase.getConnection();
        assertTrue(derbyDatabase.closeConnection(connection));
        assertTrue(connection.isClosed());
    }


    /**
     * Checks if all connections are closed after disconnecting from the database.
     */
    @Test
    @DisplayName("All database connections are closed upon disconnect")
    public void testDisconnect() {
        InitialiseTestDatabase();
        derbyDatabase.disconnect();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed());
    }


    /**
     * Ensures the proper shutdown of the Derby database.
     */
    @Test
    @DisplayName("Shutdown procedure closes the dataSource and halts the Derby database")
    public void testShutdown() {
        InitialiseTestDatabase();
        derbyDatabase.shutdown();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed());
    }


    /**
     * Verifies the database's behaviour when the connection pool size is exceeded.
     */
    @Test
    @DisplayName("Behaviour when attempting to exceed connection pool size")
    public void testSimpleExceedPoolSize() {
        InitialiseTestDatabase();

        // Get the maximum pool size from the datasource configuration
        int maxPoolSize = ((HikariDataSource) derbyDatabase.getDataSource()).getMaximumPoolSize();

        List<Connection> connections = new ArrayList<>();

        // Try to fetch more connections than the pool allows
        for (int i = 0; i < maxPoolSize + 2; i++) {
            try {
                Connection conn = derbyDatabase.getConnection();
                assertNotNull(conn);  // Ensures we actually got a connection
                connections.add(conn);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof SQLTransientConnectionException) {
                    logger.info("Expected timeout occurred: {}", e.getMessage());
                } else {
                    logger.error("Unexpected exception: {}", e.getCause().toString());
                    fail("Unexpected exception occurred when getting connection", e);
                }
            }
        }

        // Clean up - close all fetched connections
        connections.forEach(conn -> {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        });
    } /* testSimpleExceedPoolSize */


    /**
     * Validates that multiple connections can be fetched concurrently from the database.
     */
    @Test
    @DisplayName("Multiple connections can be fetched concurrently")
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
    } /* testConcurrentConnections */


    /**
     * Ensures SQL operations can perform both write and read tasks on the test database.
     */
    @Test 
    @DisplayName("Writing to and reading from the test database using SQL operations")
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
    } /* testSQLReadWrite */
}
