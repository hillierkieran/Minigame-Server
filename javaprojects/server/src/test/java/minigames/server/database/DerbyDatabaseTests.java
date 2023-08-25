package minigames.server.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DerbyDatabaseTests {

    // Mocking the HikariDataSource dependency of DerbyDatabase.
    @Mock
    private HikariDataSource dataSourceMock;

    // Injecting mocked dependencies into an instance of DerbyDatabase.
    @InjectMocks
    private DerbyDatabase derbyDatabase;

    @BeforeEach
    public void setUp() {
        // Initialising the mocks before each test.
        dataSourceMock = mock(HikariDataSource.class);
        derbyDatabase = new DerbyDatabase(dataSourceMock);
    }

    @Test
    @DisplayName("getConnection successfully retrieves connection")
    public void testGetConnection() throws SQLException {
        // Mocking the behaviour of getting a connection.
        Connection mockConnection = mock(Connection.class);
        when(dataSourceMock.getConnection()).thenReturn(mockConnection);

        // Checking if the getConnection method returns the expected connection.
        Connection result = derbyDatabase.getConnection();

        assertNotNull(result);
        verify(dataSourceMock, times(1)).getConnection();
    }

    @Test
    @DisplayName("getConnection throws RuntimeException when SQL exception occurs")
    public void testGetConnectionWithException() throws SQLException {
        // Mocking the behaviour to throw an exception when trying to get a connection.
        when(dataSourceMock.getConnection()).thenThrow(SQLException.class);

        // Checking if the expected RuntimeException is thrown.
        assertThrows(RuntimeException.class, () -> derbyDatabase.getConnection());
    }

    @Test
    @DisplayName("closeConnection successfully closes connection")
    public void testCloseConnection() throws SQLException {
        // Mocking a connection.
        Connection mockConnection = mock(Connection.class);

        // Testing the closeConnection method.
        derbyDatabase.closeConnection(mockConnection);

        // Verifying if the close method of the connection was called.
        verify(mockConnection, times(1)).close();
    }

    @Test
    @DisplayName("Should return false when there's an exception closing the connection")
    public void testCloseConnectionWithException() throws SQLException {
        // Mocking a connection to throw an exception when trying to close.
        Connection mockConnection = mock(Connection.class);
        doThrow(SQLException.class).when(mockConnection).close();

        // Assert that the method returns false, indicating an error
        assertFalse(derbyDatabase.closeConnection(mockConnection));
    }

    @Test
    @DisplayName("disconnect closes dataSource when it's not already closed")
    public void testDisconnect() {
        // Mocking the dataSource to return that it's not closed.
        when(dataSourceMock.isClosed()).thenReturn(false);

        // Testing the disconnect method.
        derbyDatabase.disconnect();

        // Verifying if the close method of the dataSource was called.
        verify(dataSourceMock, times(1)).close();
    }

    @Test
    @DisplayName("disconnect doesn't close dataSource when it's already closed")
    public void testDisconnectWithClosedDataSource() {
        // Mocking the dataSource to return that it's already closed.
        when(dataSourceMock.isClosed()).thenReturn(true);

        // Testing the disconnect method.
        derbyDatabase.disconnect();

        // Verifying that the close method of the dataSource was never called.
        verify(dataSourceMock, never()).close();
    }
}
