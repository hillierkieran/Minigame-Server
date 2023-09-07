package minigames.server.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.HikariDataSource;

import minigames.server.database.DatabaseAccessException;


/**
 * This class is responsible for conducting tests on the DerbyDatabase class.
 * All tests are conducted using JUnit and ensure the proper functioning of database operations.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabaseIntegrationTests {

    private DerbyDatabase derbyDatabase = null;
    private static final String TEST_ENV = "testEnv";
    private static final String TEST_PROP_FILE_NAME = "database/DerbyDatabaseTest.properties";
    private static final Logger logger = LogManager.getLogger(DerbyDatabaseIntegrationTests.class);


    /**
     * Setup a test environment system property
     */
    @BeforeAll
    public static void setTestEnvironment() {
        System.setProperty(TEST_ENV, "true");
    }


    private boolean isDatabaseSetup() {
        return  derbyDatabase != null &&
                !derbyDatabase.isClosed() &&
                derbyDatabase.getDataSource() != null &&
                !derbyDatabase.getDataSource().isClosed() &&
                derbyDatabase.getPropFileName() != null &&
                derbyDatabase.getPropFileName().equals(TEST_PROP_FILE_NAME);
    }


    /**
     * Re-initialises the test database before each test.
     */
    @BeforeEach
    public void InitialiseTestDatabase() {
        // Restart database in case it was shutdown in a test
        if (!isDatabaseSetup()) {
            derbyDatabase = new DerbyDatabase(TEST_PROP_FILE_NAME);
        }
    }


    /**
     * Builds a sample table 'TEST_TABLE' in the database.
     */
    public void createTestTable() {
        // Restart database in case it has been shutdown
        InitialiseTestDatabase();
        // Delete any leftover data from previous tests
        deleteTestTable();
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement()
        ) {
            // Create the TEST_TABLE
            String createTableSQL = "CREATE TABLE TEST_TABLE(" +
                                        "ID INT PRIMARY KEY, " +
                                        "NAME VARCHAR(255)" +
                                    ")";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            logger.error("SQL Error encountered during operation: ", e);
        }
    }


    private int getRandomKey() {
        Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);
    }


    /**
     * Cleans up resources after each test, such as closing database connections and clearing data.
     */
    @AfterEach
    public void tearDownTestDatabase() {
        // Restart database in case it has been shutdown so that we can...
        InitialiseTestDatabase();
        // Delete any leftover data from previous tests
        deleteTestTable();
        // Shutdown the database
        derbyDatabase.shutdown();
        // Nullify the database instance
        derbyDatabase = null;
    }


    /**
     * Builds a sample table 'TEST_TABLE' in the database.
     */
    public void deleteTestTable() {
        // Restart database in case it has been shutdown
        InitialiseTestDatabase();
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement()
        ) {
            // Check if the TEST_TABLE exists
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet result = metadata.getTables(null, null, "TEST_TABLE", null);
            if (result.next()) {
                // Drop the TEST_TABLE
                String dropTableSQL = "DROP TABLE IF EXISTS TEST_TABLE";
                stmt.execute(dropTableSQL);
            }
        } catch (SQLException e) {
            logger.error("SQL Error encountered during operation: ", e);
        }
    }


    static String getDatabaseDirectoryName() {
        Properties testProperties = new Properties();
        String testDbDirectoryName;
        try (
            InputStream input = DerbyDatabaseIntegrationTests.class.getClassLoader()
                .getResourceAsStream(TEST_PROP_FILE_NAME)
        ) {
            testProperties.load(input);
            String jdbcUrl = testProperties.getProperty("db.jdbcUrl");
            if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                throw new IllegalArgumentException(
                    "db.jdbcUrl not found in properties file!"
                );
            }
            // Extracting directory name from the JDBC URL
            return jdbcUrl.split(":")[2].split(";")[0];
        } catch (IOException ex) {
            logger.error("Error loading properties file: ", ex);
            throw new IllegalStateException(
                "Failed to load test properties, cleanup aborted."
            );
        }
    }


    /**
     * WARNING! Editing this method could be dangerous. 
     * This method removes all test database files and directories after all tests are complete.
     */
    @AfterAll
    public static void cleanUp() {
        // Step 1: Check our custom system property to ensure we're in a test environment
        if (!"true".equals(System.getProperty(TEST_ENV))) {
            throw new IllegalStateException("Not in a test environment! Cleanup aborted.");
        }
        // Step 2: Get test database directory name to minimize chance of incorrect deletions
        String testDbDirectoryName = getDatabaseDirectoryName();
        // Step 3: Derive the full path for the test database directory
        Path testDbPath = Paths.get(System.getProperty("user.dir"), testDbDirectoryName);
        // Step 4: Double-check that we are about to delete the correct directory
        if (!testDbPath.endsWith(testDbDirectoryName)) {
            throw new IllegalStateException(
                "Trying to delete an unexpected directory! Cleanup aborted."
                );
        }
        // Step 5: Check if the directory exists before attempting deletion
        if (Files.exists(testDbPath)) {
            logger.info("Preparing to delete test database directory: " + testDbPath);
            try {
                // Step 6: Delete all nested files and directories in reverse order 
                // to ensure that directories are empty before they get deleted
                Files.walk(testDbPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        logger.debug("Deleting file or directory: " + file.getPath());
                        file.delete();
                    });
            } catch (IOException e) {
                logger.error("I/O Error during deletion: ", e);
            }
        }
        // Step 7: Finally, clear the test environment system property
        System.clearProperty(TEST_ENV);
    } /* cleanUp */


    // Begin tests


    @Test // 1
    @DisplayName("Testing if initialisation methods executed correctly")
    public void testTestDatabaseInitialisation() {
        // Check if the system property 'testEnv' was set to 'true'
        assertEquals("true", System.getProperty(TEST_ENV), 
            "System property 'testEnv' should be set to 'true'");
        // Check if the derbyDatabase object was instantiated using the test properties file
        assertNotNull(derbyDatabase, "The derbyDatabase object should be initialised");
        assertEquals(TEST_PROP_FILE_NAME, derbyDatabase.getPropFileName(), 
            "The derbyDatabase should be initialised using the test properties file");
    }


    /**
     * Testing retrieval of the DerbyDatabase singleton instance.
     */
    @Test // 2
    @DisplayName("Singleton getInstance() method initializes without errors")
    public void testSingletonInstanceRetrieval() {
        tearDownTestDatabase();
        try (DerbyDatabase db = DerbyDatabase.getInstance()) {
            assertNotNull(db, "The db object should be initialised");
            assertEquals(db.getDefaultPropFileName(), db.getPropFileName(), 
                "The derbyDatabase should be initialised using the default properties file");
        } catch (Exception e) {
            fail("Error retrieving Derby database singleton instance using getInstance()", e);
        }
    }


    /**
     * Test for building the sample TEST_TABLE.
     */
    @DisplayName("Building the TEST_TABLE successfully in the database")
    @Test // 3
    public void testBuildingTable() {
        try {
            createTestTable();

            // After creating the table, check if it exists and is structured as expected.
            try (
                Connection connection = derbyDatabase.getConnection()
            ) {
                // Check if table exists
                ResultSet tables = connection.getMetaData()
                    .getTables(null, null, "TEST_TABLE", null);
                assertTrue(tables.next(), "TEST_TABLE should exist");

                // Check table structure
                ResultSet columns = connection.getMetaData()
                    .getColumns(null, null, "TEST_TABLE", null);
                List<String> columnNames = new ArrayList<>();
                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }

                List<String> expectedColumns = List.of("ID", "NAME");
                assertTrue(
                    columnNames.containsAll(expectedColumns),
                    "Columns in TEST_TABLE do not match expectations"
                );
            }
        } catch (Exception e) {
            fail("Error building Derby database table", e);
        }
    }


    /**
     * Tests the scenario when a properties file is missing.
     */
    @Test // 4
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
    @Test // 5
    @DisplayName("Database initializes with the expected TEST_TABLE schema")
    public void testDatabaseInitialisation() throws SQLException {
        Connection connection = derbyDatabase.getConnection();
        ResultSet resultSet = connection.getMetaData().getTables(null, null, "TEST_TABLE", null);
        assertTrue(resultSet.next());
        derbyDatabase.closeConnection(connection);
    }


    /**
     * Ensures the database connection can be successfully fetched.
     */
    @Test // 6
    @DisplayName("Database connection can be successfully retrieved")
    public void testGetConnection() throws SQLException {
        Connection connection = derbyDatabase.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        derbyDatabase.closeConnection(connection);
    }


    /**
     * Ensures the database connection can be successfully closed.
     */
    @Test // 7
    @DisplayName("Database connection can be successfully closed")
    public void testCloseConnection() throws SQLException {
        Connection connection = derbyDatabase.getConnection();
        assertTrue(derbyDatabase.closeConnection(connection));
        assertTrue(connection.isClosed());
    }


    /**
     * Checks if all connections are closed after disconnecting from the database.
     */
    @Test // 8
    @DisplayName("All database connections are closed upon disconnect")
    public void testDisconnect() {
        derbyDatabase.disconnect();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed(),
            "Database source should be closed after disconnect.");
        assertThrows(DatabaseAccessException.class,
            () -> derbyDatabase.getConnection(),
            "Expected DatabaseAccessException to be thrown when getting connection after disconnect.");
    }


    /**
     * Ensures the proper shutdown of the Derby database.
     */
    @Test // 9
    @DisplayName("Shutdown procedure closes the dataSource and halts the Derby database")
    public void testShutdown() {
        derbyDatabase.shutdown();
        assertTrue(((HikariDataSource) derbyDatabase.getDataSource()).isClosed());
        assertTrue(derbyDatabase.isClosed());
        // Attempt to make a direct connection to Derby. This should fail if Derby has shut down.
        assertThrows(SQLException.class, () -> {
            try (
                Connection conn = DriverManager.getConnection(
                    "jdbc:derby:" + getDatabaseDirectoryName()
                )
            ) {
                // Connection attempt
            }
        });
    }


    /**
     * Verifies the database's behaviour when the connection pool size is exceeded.
     */
    @Test // 10
    @DisplayName("Behaviour when attempting to exceed connection pool size")
    public void testExceedPoolSize() {

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
    @Test // 11
    @DisplayName("Stress test for fetching multiple concurrent connections")
    public void testConcurrentConnectionsStressTest() throws SQLException {
        int numOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        CountDownLatch latch = new CountDownLatch(numOfThreads);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numOfThreads; i++) {
            executorService.submit(() -> {
                try (
                    Connection connection = derbyDatabase.getConnection();
                ) {
                    Thread.sleep(50); // Simulate workload
                } catch (DatabaseAccessException dae) {
                    // Catching database access exceptions here can give
                    // insight into any issues that arise from the stress test,
                    // like timeout exceptions or connection failures
                    exceptions.add(dae);
                } catch (InterruptedException | SQLException e) {
                    exceptions.add(e);
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executorService.shutdown();

        assertTrue(exceptions.isEmpty(),
            "Exceptions were thrown during concurrent execution: " +
            exceptions.stream()
                .map(Exception::toString)
                .collect(Collectors.joining(", ")));
    } /* testConcurrentConnectionsStressTest */


    /**
     * Ensures SQL operations can perform both write and read tasks on the test database.
     */
    @Test // 12
    @DisplayName("Writing to and reading from the test database using SQL operations")
    public void testSQLReadWrite() throws SQLException {
        int key = getRandomKey();
        createTestTable();
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            // Insert data
            String insertSQL = "INSERT INTO TEST_TABLE VALUES (" + key + ", 'TestName')";
            stmt.execute(insertSQL);

            // Query data
            String querySQL = "SELECT NAME FROM TEST_TABLE WHERE ID = " + key;
            ResultSet result = stmt.executeQuery(querySQL);
            assertTrue(result.next());
            assertEquals("TestName", result.getString("NAME"));
        }
    } /* testSQLReadWrite */


    @Test // 13
    @DisplayName("Testing UPDATE operation on the TEST_TABLE")
    public void testSQLUpdate() throws SQLException {
        int key = getRandomKey();
        createTestTable();
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            // Initial insert
            String insertSQL = "INSERT INTO TEST_TABLE VALUES (" + key +", 'OriginalName')";
            stmt.execute(insertSQL);

            // Updating the inserted record
            String updateSQL = "UPDATE TEST_TABLE SET NAME = 'UpdatedName' WHERE ID = " + key;
            stmt.execute(updateSQL);

            // Fetching and verifying the update
            String querySQL = "SELECT NAME FROM TEST_TABLE WHERE ID = " + key;
            ResultSet result = stmt.executeQuery(querySQL);
            assertTrue(result.next());
            assertEquals("UpdatedName", result.getString("NAME"));
        }
    } /* testSQLUpdate */


    @Test // 14
    @DisplayName("Handling duplicate primary key insertion in TEST_TABLE")
    public void testDuplicatePrimaryKey() {
        int key = getRandomKey();
        createTestTable();
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement();
        ) {

            String insertSQL1 = "INSERT INTO TEST_TABLE VALUES (" + key +", 'TestName1')";
            stmt.execute(insertSQL1);

            String insertSQL2 = "INSERT INTO TEST_TABLE VALUES (" + key +", 'TestName2')";
            stmt.execute(insertSQL2);  // This should fail

            // If it doesn't fail, this line will execute and fail the test
            fail("Expected an SQLException to be thrown");
        } catch (SQLException e) {
            // Confirming that the exception is due to a duplicate primary key
            assertTrue(e.getMessage().contains("duplicate key"));
        }
    } /* testSQLDuplicatePrimaryKey */


    @Test // 15
    @DisplayName("Race condition concurrent inserts into the same row")
    public void testRaceCondition() {
        int key = getRandomKey();
        createTestTable();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable insertTask = () -> {
            try {
                latch.await();  // Ensure both threads start at the same time
                try (
                    Connection connection = derbyDatabase.getConnection();
                    Statement stmt = connection.createStatement()
                ) {
                    stmt.execute("INSERT INTO TEST_TABLE VALUES (" + key + ", 'TestName')");
                }
            } catch (InterruptedException | SQLException e) {
                logger.error("Error in concurrent insert", e);
            }
        };

        executor.submit(insertTask);
        executor.submit(insertTask);

        latch.countDown();  // Start both threads
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // Only one of them should have succeeded, and the other should throw an exception.
        try (
            Connection connection = derbyDatabase.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TEST_TABLE WHERE ID = " + key)
        ) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        } catch (SQLException e) {
            logger.error("Error verifying concurrent inserts", e);
        }
    } /* testRaceCondition */
}
