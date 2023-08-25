package minigames.server.database;

import java.sql.Connection;

/**
 * Represents a generic database connection interface.
 * This interface provides a contract for establishing and closing connections
 * to a specific database. Classes implementing this interface can use ANY database
 * system and connection pooling mechanism.
 */
public interface DatabaseConnection {

    /**
     * Retrieves a database connection for client requests.
     * Depending on the implementation, this method might return a new connection
     * every time or reuse a connection from a pool.
     *
     * @return An active {@link Connection} object to interact with the database.
     */
    Connection getConnection();

    /**
     * Closes the provided database connection.
     * Implementations using connection pooling might not physically close the connection
     * but might return it to the pool for reuse. Returns true if the operation is successful
     * and false otherwise.
     *
     * @param connection The {@link Connection} object that needs to be closed or returned to the pool.
     * @return true if the connection is closed or returned to the pool successfully, false otherwise.
     */
    boolean closeConnection(Connection connection);
}
