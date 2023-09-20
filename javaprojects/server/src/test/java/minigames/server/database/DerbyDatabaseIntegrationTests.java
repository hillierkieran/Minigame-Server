package minigames.server.database;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.HikariDataSource;

import minigames.server.utilities.Utilities;


/**
 * Integration tests for DerbyDatabase operations.
 * Validates database CRUD operations, connectivity, concurrency, and cleanup.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabaseIntegrationTests {

    private static final Logger logger = LogManager.getLogger(DerbyDatabaseIntegrationTests.class);
    private static final String TEST_ENV = "testEnv";
    private static final String TEST_DB_PROPERTIES = "database/DerbyDatabaseTest.properties";
    private static final String TEST_TABLE_NAME = "TEST_TABLE";
    private static String testDatabasePath;

    private DerbyDatabase testDatabase;
    private ExampleTable testTable;


    @BeforeAll
    public static void initialise() {
        System.setProperty(TEST_ENV, "true");
        testDatabasePath = Utilities.getProperties(TEST_DB_PROPERTIES)
                                    .getProperty("db.jdbcUrl")
                                    .split(":")[2].split(";")[0];
        deleteTestDatabaseFiles();
    }

    @BeforeEach
    public void setup() {
        testDatabase = new DerbyDatabase(TEST_DB_PROPERTIES);
        testTable = new ExampleTable(testDatabase, TEST_TABLE_NAME);
        if (testTable.tableExists()) {
            testTable.clearTable();
        }
        testTable.createTable();
    }

    private boolean isDatabaseSetupCorrectly() {
        return  testDatabase != null &&
                testDatabase.getPropFileName() != null &&
                testDatabase.getPropFileName()
                    .equals(TEST_DB_PROPERTIES);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // restart so we can clear table from database
        if (testDatabase == null ||
            !testDatabase.getPropFileName().equals(TEST_DB_PROPERTIES)) {
            testDatabase = new DerbyDatabase(TEST_DB_PROPERTIES);
        }
        testDatabase.destroyAllRegisteredTestTables();
        testDatabase.close();
        testDatabase = null;
        testTable = null;
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(TEST_ENV);
    }

    // Remove any/all test database files and directories
    private static void deleteTestDatabaseFiles() {
        int retries = 10;
        /* 
         * WARNING! Editing the following logic could be dangerous. 
         */
        // Step 1: Check our custom system property to ensure we're in a test environment
        if (!"true".equals(System.getProperty(TEST_ENV))) {
            logger.error("Not in a test environment! Cleanup aborted.");
            return;
        }
        // Step 2: Derive the full path for the test database directory
        Path testDbPath = Paths.get(System.getProperty("user.dir"), testDatabasePath);
        // Step 3: Double-check that we are about to delete the correct directory
        if (!testDbPath.endsWith(testDatabasePath)) {
            logger.error("Trying to delete an unexpected directory! Cleanup aborted.");
            return;
        }
        // Step 4: Check if the directory exists from a previous test and attempt deletion
        if (Files.exists(testDbPath)) {
            logger.debug("Found test database directory: " + testDbPath + ". Preparing to delete.");
            try {
                // Step 5: Delete all nested files and directories in reverse order 
                // to ensure that directories are empty before they get deleted
                Files.walk(testDbPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        logger.debug("Deleting file or directory: " + file.getPath());
                        file.delete();
                    });
                // Add a check to make sure the directory is indeed deleted
                while(retries > 0 && Files.exists(testDbPath)) {
                    Thread.sleep(500); // wait for 500ms
                    retries--;
                }
                if(Files.exists(testDbPath)) {
                    logger.error("Unable to delete the database directory after multiple retries.");
                }
            } catch (IOException e) {
                logger.error("I/O Error during deletion: ", e);
            } catch (InterruptedException ie) {
                logger.error("Interrupted during wait: ", ie);
                Thread.currentThread().interrupt(); // Handle the interrupt
            }
        }
    }


    // Begin tests

    @Test
    public void testTestSetup() {
        assertEquals("true", System.getProperty(TEST_ENV));
        assertNotNull(testDatabase);
        assertEquals(TEST_DB_PROPERTIES, testDatabase.getPropFileName());
        assertNotNull(testTable);
        assertTrue(testTable.tableExists());
        assertTrue(testDatabase.isTableRegistered(testTable));
    }


    @Test
    public void testSingleton() throws SQLException {
        try (
            DerbyDatabase db1 = DerbyDatabase.getInstance();
            DerbyDatabase db2 = DerbyDatabase.getInstance();
        ) {
            assertNotNull(db1);
            assertNotNull(db2);
            assertSame(db1, db2);
        }
    }


    @Test
    public void testMissingPropertiesFile() {
        Exception exception = assertThrows(Exception.class, 
            () -> new DerbyDatabase("missing.properties"));
        if (exception.getCause() != null) {
            assertTrue(exception.getCause() instanceof RuntimeException);
        } else {
            assertTrue(exception instanceof RuntimeException);
        assertTrue(exception.getMessage().contains("Unable to find"));
        }
    }


    @Test
    public void testGetConnection() throws SQLException {
        try (Connection connection = testDatabase.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }


    @Test
    public void testGetConnection_WhenPoolSizeExceeded() {
        List<Connection> connections = new ArrayList<>();
        // Get the maximum pool size from the datasource configuration
        int maxPoolSize = ((HikariDataSource) testDatabase.getDataSource()).getMaximumPoolSize();
        // Try to get more connections than the pool allows
        for (int i = 0; i < maxPoolSize + 1; i++) {
            try {
                Connection conn = testDatabase.getConnection();
                assertNotNull(conn);  // Ensure we actually got a connection
                connections.add(conn);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof SQLTransientConnectionException) {
                    logger.debug("Expected timeout occurred: {}", e.getMessage());
                } else {
                    logger.error("Unexpected exception: {}", e.getCause().toString());
                    fail("Unexpected exception occurred when getting connection", e);
                }
            }
        }
        // Clean up - close all fetched connections
        connections.forEach(conn -> {
            try { conn.close(); } 
            catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        });
    }


    @Test
    public void testGetConnection_WithConcurrency() throws SQLException {
        int numOfThreads = 20;
        ExecutorService threads = Executors.newFixedThreadPool(numOfThreads); // make pool of threads
        CountDownLatch latch = new CountDownLatch(numOfThreads); // make threads act simultaneously
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>()); // stores exceptions
        // Submit task to each thread
        for (int i = 0; i < numOfThreads; i++) {
            threads.submit(() -> {
                try (Connection connection = testDatabase.getConnection()) {
                    Thread.sleep(50); // Simulate work
                } catch (DatabaseAccessException dae) {
                    exceptions.add(dae);
                } catch (InterruptedException | SQLException e) {
                    exceptions.add(e);
                }
                latch.countDown(); // Count down latch to say that this thread has completed its task
            });
        }
        // Wait for all threads finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        threads.shutdown();
        // Check that no exceptions were thrown during execution
        assertTrue(exceptions.isEmpty(),
            "Exceptions were thrown during concurrent execution: " +
            exceptions.stream()
                .map(Exception::toString)
                .collect(Collectors.joining(", ")));
    }


    @Test
    public void testTableCreation() {
        ExampleTable newTable = null;
        String newTableName = "new_" + TEST_TABLE_NAME;
        try {
            newTable = new ExampleTable(testDatabase, newTableName);
            newTable.createTable();
            assertTrue(newTable.tableExists());
        } finally {
            if (newTable != null) {
                newTable.destroyTable();
            }
        }
    }


    private String columnsToString(List<String> columnList) {
        StringBuilder list = new StringBuilder("{");
        for (int i = 0; i < columnList.size(); i++) {
            list.append(columnList.get(i));
            if (i != columnList.size() - 1) {
                list.append(", ");
            }
        }
        return list.append("}.").toString();
    }


    @Test
    public void testTableStructure() throws SQLException {
        try (Connection connection = testDatabase.getConnection()) {
            // Get table column structure
            ResultSet columns = connection.getMetaData()
                .getColumns(null, null, testTable.getTableName().toUpperCase(), null);
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            // Check table column structure
            List<String> expectedColumns = List.of(
                testTable.getColumnExample1().toUpperCase(),
                testTable.getColumnExample2().toUpperCase()
            );
            logger.info("expected columns: " + columnsToString(expectedColumns));
            logger.info("retrieved columns: " + columnsToString(columnNames));
            logger.info("retrieved columns == expected columns: " + columnNames.containsAll(expectedColumns));
            assertTrue(
                columnNames.containsAll(expectedColumns),
                "Columns in " + testTable.getTableName() + " do not match expectations"
            );
        }
    }


    private String recordsToString(List<ExampleRecord> resultList) {
        StringBuilder list = new StringBuilder("{");
        for (int i = 0; i < resultList.size(); i++) {
            list.append(resultList.get(i).toString());
            if (i != resultList.size() - 1) {
                list.append(", ");
            }
        }
        return list.append("}.").toString();
    }


    @Test
    public void testCRUDOperations() throws SQLException {
        ExampleRecord record1 = new ExampleRecord();
        ExampleRecord record2 = new ExampleRecord();
        record2.setValue(record1.getValue()); // match values to test retrieveMany
        logger.info("input records: " + recordsToString(Arrays.asList(record1, record2)));
        // CREATE:
        testTable.create(record1);
        testTable.create(record2);
        // RETRIEVE ONE:
        ExampleRecord retrievedRecord1 = testTable.retrieveOne(record1);
        ExampleRecord retrievedRecord2 = testTable.retrieveOne(record2);
        assertEquals(record1.getKey(),   retrievedRecord1.getKey());
        assertEquals(record1.getValue(), retrievedRecord1.getValue());
        assertEquals(record2.getKey(),   retrievedRecord2.getKey());
        assertEquals(record2.getValue(), retrievedRecord2.getValue());
        // RETRIEVE MANY:
        List<ExampleRecord> manyRecords = testTable.retrieveMany(record1);
        logger.info("retrieveMany results: " + recordsToString(manyRecords));
        logger.info("retrieveMany == input?: " + manyRecords.containsAll(Arrays.asList(record1, record2)));
        assertTrue(manyRecords.containsAll(Arrays.asList(record1, record2)));
        // RETRIEVE ALL:
        List<ExampleRecord> allRecords = testTable.retrieveAll();
        logger.info("retrieveAll results: " + recordsToString(allRecords));
        logger.info("retrieveAll == input?: " + allRecords.containsAll(Arrays.asList(record1, record2)));
        assertTrue(allRecords.containsAll(Arrays.asList(record1, record2)));
        // UPDATE:
        record1.setValue(1);
        testTable.update(record1);
        ExampleRecord updatedRecord1 = testTable.retrieveOne(record1);
        assertEquals(1, updatedRecord1.getValue());
        // DELETE:
        testTable.delete(record1);
        testTable.delete(record2);
        assertNull(testTable.retrieveOne(record1));
        assertNull(testTable.retrieveOne(record2));
    }


    @Test
    public void testInsert_WhenDuplicateKey() {
        ExampleRecord record1 = new ExampleRecord("Key", 1);
        ExampleRecord record2 = new ExampleRecord("Key", 2);
        testTable.create(record1);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            testTable.create(record2);
        });
        assertTrue(exception.getMessage().contains("duplicate key"));
    }


    @Test
    public void testInsert_WithRaceCondition() throws InterruptedException {
        int numOfThreads = 2;
        ExecutorService threads = Executors.newFixedThreadPool(numOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1); // signals threads to start
        CountDownLatch doneLatch = new CountDownLatch(numOfThreads); // signals threads are done
        List<Boolean> outcomes = Collections.synchronizedList(new ArrayList<>());
        // Submit task to each thread
        for (int i = 0; i < numOfThreads; i++) {
            threads.submit(() -> {
                try {
                    startLatch.await(); // wait for the start signal
                    testTable.create(new ExampleRecord("identical keys", 123));
                    outcomes.add(true); // true indicates a successful insertion
                } catch (DatabaseAccessException dae) {
                    outcomes.add(false); // false indicates a failed insertion
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown(); // signal that this thread is done
                }
            });
        }
        startLatch.countDown(); // Start all threads simultaneously
        doneLatch.await(); // wait for all threads to complete
        // Assert that only exactly one thread succeeded
        long successes = outcomes.stream().filter(b -> b).count();
        assertEquals(1, successes);
    }


    @Test
    public void testCloseConnection() throws SQLException {
        try (Connection connection = testDatabase.getConnection()) {
            assertTrue(testDatabase.closeConnection(connection));
            assertTrue(connection.isClosed());
        }
    }


    @Test
    public void testDisconnect() {
        testDatabase.disconnect();
        assertTrue(testDatabase.isDisconnected());
    }


    @Test
    public void testShutdown() throws DatabaseShutdownException {
        testDatabase.shutdown();
        assertTrue(testDatabase.isDisconnected());
        assertTrue(testDatabase.isClosed());
    }

    @Test
    public void testBackupAndRestore() throws IOException, SQLException {
        // Setup: Create initial records and insert them
        ExampleRecord record1 = new ExampleRecord("key1", 10);
        ExampleRecord record2 = new ExampleRecord("key2", 20);
        testTable.create(record1);
        testTable.create(record2);

        // Perform backup
        testTable.backup();

        // Modify the table
        testTable.delete(record1);                       // delete record1
        testTable.update(new ExampleRecord("key2", 88)); // update record2's value

        // Restore the table from backup
        testTable.restore();

        // Check if data matches the initial state
        ExampleRecord restoredRecord1 = testTable.retrieveOne(record1);
        ExampleRecord restoredRecord2 = testTable.retrieveOne(record2);

        assertNotNull(restoredRecord1);
        assertEquals(record1.getValue(), restoredRecord1.getValue());
        assertEquals(record2.getValue(), restoredRecord2.getValue());
    }
}
