package minigames.server.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents the main interface for a database, defining common database operations.
 */
public interface Database extends AutoCloseable{

    /**
     * Gets a connection from the connection pool or the database directly.
     *
     * @return a {@link Connection} object for database interaction.
     */
    public Connection getConnection();

    /**
     * Closes the provided connection, returning it back to the connection pool
     * or releasing the resource.
     *
     * @param connection The {@link Connection} to be closed.
     * @return true if the connection is successfully closed, false otherwise.
     */
    public boolean closeConnection(Connection connection);

    /**
     * Closes any resources related to the database, such as connection pools.
     *
     * @throws Exception if any error occurs during the closing process.
     */
    @Override
    public void close() throws Exception;
}
