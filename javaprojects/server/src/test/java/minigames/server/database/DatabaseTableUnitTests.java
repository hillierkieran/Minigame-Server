package minigames.server.database;

import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.*;
import static org.mockito.Mockito.*;


/**
 * Unit tests for DatabaseTable operations.
 * Validates table creation, data CRUD operations, and backups.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DatabaseTableUnitTests {

    private static final String TEST_ENV = "testEnv";
    private static final String TEST_TABLE_NAME = "TEST_TABLE";

    @Mock
    private Database mockDatabase;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private DatabaseMetaData mockDatabaseMetaData;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private File mockFile;

    private ExampleTable testTable;


    @BeforeAll
    public static void initialise() {
        System.setProperty(TEST_ENV, "true");
    }

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        mockBackupExists(false); // default: no backup file
        mockTableExists(true);   // default: table exists
        mockQueryResults(1);     // default: one result found
        testTable = new ExampleTable(mockDatabase, TEST_TABLE_NAME);
    }

    private void mockBackupExists(boolean exists) throws Exception {
        when(mockFile.exists()).thenReturn(exists);
        when(mockFile.mkdirs()).thenReturn(true);
    }

    private void mockTableExists(boolean exists) throws Exception {
        when(mockConnection.getMetaData()).thenReturn(mockDatabaseMetaData);
        when(mockDatabaseMetaData.getTables(any(),any(),any(),any())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(exists);
    }

    private void mockQueryResults(int i) throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.getString(anyString())).thenReturn("test");
        when(mockResultSet.getInt(anyString())).thenReturn(1);
        switch (i) {
            case 0:
                when(mockResultSet.next()).thenReturn(true, false); 
                break;
            case 1:
                when(mockResultSet.next()).thenReturn(true, true, false); 
                break;
            case 2:
                when(mockResultSet.next()).thenReturn(true, true, true, false); 
                break;
            case 10:
                when(mockResultSet.next()).thenReturn(
                    true, true, true, true, true,  true,
                    true, true, true, true, true,  false);
                break;
            default:
                throw new IllegalArgumentException("Unsupported value of i: " + i);
        }
    }

    @AfterEach
    public void tearDown() {
        if (testTable == null) {
            testTable = new ExampleTable(mockDatabase, TEST_TABLE_NAME);
        }
        testTable.destroyTable();
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(TEST_ENV);
    }


    // Begin tests

    @Test
    public void testConstructor() throws Exception {
        String newTableName = "NEW_" + TEST_TABLE_NAME;
        ExampleTable newTable = new ExampleTable(mockDatabase, newTableName);
        verify(mockDatabase).registerTable(newTable);
        assertEquals(newTable.getTableName(), newTableName);
    }

    @Test
    public void testCreateTable() throws Exception {
        mockTableExists(false);
        testTable.createTable();
        verify(mockConnection)
            .prepareStatement(contains("CREATE TABLE " + TEST_TABLE_NAME));
    }


    @Test
    public void testRestore() throws Exception {
        mockBackupExists(true);
        testTable.restore(mockFile);
        verify(mockConnection, atLeastOnce())
            .prepareStatement(contains("SYSCS_IMPORT_TABLE"));
    }


    @Test
    public void testTableExists_WhenTableExists() throws Exception {
        mockTableExists(true);
        assertTrue(testTable.tableExists());
    }


    @Test
    public void testTableExists_WhenTableDoesNotExist() throws Exception {
        mockTableExists(false);
        assertFalse(testTable.tableExists());
    }


    @Test
    public void testBackup() throws Exception {
        mockTableExists(true);
        mockBackupExists(false);
        testTable.backup(mockFile);
        verify(mockFile).mkdirs();
        verify(mockConnection).prepareStatement(contains("SYSCS_EXPORT_TABLE"));
    }


    @Test
    public void testCreate() throws Exception {
        testTable.create(new ExampleRecord("test", 1));
        verify(mockConnection).prepareStatement(contains("INSERT INTO"));
        verify(mockStatement).setObject(1, "test");
        verify(mockStatement).setObject(2, 1);
    }


    @Test
    public void testUpdate() throws Exception {
        testTable.update(new ExampleRecord("test", 1));
        verify(mockConnection).prepareStatement(contains("UPDATE"));
        verify(mockStatement).setObject(1, 1);
        verify(mockStatement).setObject(2, "test");
    }


    @Test
    public void testRetrieveOne_WhenNothingFound() throws Exception {
        mockQueryResults(0);
        assertNull(testTable.retrieveOne(new ExampleRecord("test", 0)));
    }


    @Test
    public void testRetrieveOne_WhenRecordFound() throws Exception {
        mockQueryResults(1);
        assertNotNull(testTable.retrieveOne(new ExampleRecord("test", 0)));
    }


    @Test
    public void testRetrieveMany_WhenNothingFound() throws Exception {
        mockQueryResults(0);
        List<ExampleRecord> results = testTable.retrieveMany(new ExampleRecord(null, 0));
        assertTrue(results.isEmpty());
    }


    @Test
    public void testRetrieveMany_WhenRecordsFound() throws Exception {
        int numOfResults = 2;
        mockQueryResults(numOfResults);
        List<ExampleRecord> results = testTable.retrieveMany(new ExampleRecord(null, 0));
        assertEquals(numOfResults, results.size());
    }


    @Test
    public void testRetrieveAll_WhenNothingFound() throws Exception {
        mockQueryResults(0);
        List<ExampleRecord> results = testTable.retrieveAll();
        assertTrue(results.isEmpty());
    }


    @Test
    public void testRetrieveAll_WhenRecordsFound() throws Exception {
        int numOfResults = 10;
        mockQueryResults(numOfResults);
        List<ExampleRecord> results = testTable.retrieveAll();
        assertEquals(numOfResults, results.size());
    }


    @Test
    public void testDelete() throws Exception {
        testTable.delete(new ExampleRecord("test", 1));
        verify(mockConnection).prepareStatement(contains("DELETE"));
    }


    @Test
    public void testDestroyTable() throws Exception {
        mockTableExists(true);
        testTable.destroyTable();
        verify(mockDatabase).unregisterTable(testTable);
        verify(mockConnection).prepareStatement(contains("DROP TABLE " + TEST_TABLE_NAME));
    }
}
