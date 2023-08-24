package minigames.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDatabaseAPI {
    private static final String DB_URL = "jdbc:derby:myDb;create=true";
    private Connection connection;

    public DerbyDatabaseAPI() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
