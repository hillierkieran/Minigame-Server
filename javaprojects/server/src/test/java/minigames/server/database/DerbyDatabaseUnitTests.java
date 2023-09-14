package minigames.server.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.zaxxer.hikari.HikariDataSource;


/**
 * Unit tests for DerbyDatabase class.
 * Ensures proper database connections, shutdowns, and related operations.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class DerbyDatabaseUnitTests {

    private static final String TEST_ENV = "testEnv";

    @Mock
    private HikariDataSource mockDataSource;
    @Mock
    private Connection mockConnection;
    @Mock
    private ExampleTable mockTable;

    private DerbyDatabase derbyDatabase;

    @BeforeAll
    public static void initialise() {
        System.setProperty(TEST_ENV, "true");
    }

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        derbyDatabase = new DerbyDatabase(mockDataSource);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (derbyDatabase != null && !derbyDatabase.isClosed()) {
            derbyDatabase.close();
        }
        derbyDatabase = null;
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(TEST_ENV);
    }


    // Begin tests

    @Test
    public void testSingleton() throws SQLException {
        try (
            DerbyDatabase instance1 = DerbyDatabase.getInstance();
            DerbyDatabase instance2 = DerbyDatabase.getInstance();
        ) {
            assertSame(instance1, instance2);
        }
    }


    @Test
    public void testMissingPropertiesFile() throws SQLException {
        assertThrows(RuntimeException.class,
            () -> new DerbyDatabase("missing.properties"));
    }


    @Test
    public void testRegisterTable() {
        derbyDatabase.registerTable(mockTable);
        assertTrue(derbyDatabase.getRegisteredTables().contains(mockTable));
    }


    @Test
    public void testGetConnection() throws DatabaseAccessException, SQLException  {
        Connection result = derbyDatabase.getConnection();
        assertNotNull(result);
        verify(mockDataSource, times(1)).getConnection();
        assertEquals(result, mockConnection);
    }


    @Test
    public void testGetConnection_WithException() throws DatabaseAccessException, SQLException  {
        when(mockDataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(DatabaseAccessException.class,
            () -> derbyDatabase.getConnection());
    }


    @Test
    public void testCloseConnection() throws DatabaseAccessException, SQLException {
        derbyDatabase.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }


    @Test
    public void testCloseConnection_WithException() throws SQLException {
        doThrow(SQLException.class).when(mockConnection).close();
        assertThrows(DatabaseAccessException.class,
            () -> derbyDatabase.closeConnection(mockConnection));
    }


    @Test
    public void testDisconnect() {
        when(mockDataSource.isClosed()).thenReturn(false);
        derbyDatabase.disconnect();
        verify(mockDataSource, times(1)).close();
    }


    @Test
    public void testDisconnect_WithClosedDataSource() {
        when(mockDataSource.isClosed()).thenReturn(true);
        derbyDatabase.disconnect();
        verify(mockDataSource, never()).close();
    }


    @Test
    public void testMultipleShutdown() throws DatabaseShutdownException {
        derbyDatabase.shutdown();
        assertDoesNotThrow(() -> derbyDatabase.shutdown());
    }
}
