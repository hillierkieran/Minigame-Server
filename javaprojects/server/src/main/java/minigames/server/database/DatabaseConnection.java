package minigames.server.database;

import java.sql.Connection;

public interface DatabaseConnection {
    Connection getConnection();
    void closeConnection(Connection connection);
}
