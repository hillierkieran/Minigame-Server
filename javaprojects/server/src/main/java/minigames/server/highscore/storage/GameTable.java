package minigames.server.highscore;

import minigames.server.database.Database;
import minigames.server.database.DatabaseTable;
import minigames.server.database.DatabaseAccessException;
import minigames.server.highscore.GameMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


public class GameTable implements DatabaseTable<GameMetadata> {

    private Database database;


    public GameTable(Database database) {
        this.database = database;
        ensureTableExists();
    }


    @Override
    public void ensureTableExists() {
        try (
            Connection connection = database.getConnection()
        ) {
            ResultSet resultSet = connection.getMetaData().getTables(
                null, null, "gameMetadata", null
            );
            if (!resultSet.next()) {
                // Table does not exist, create it
                try (
                    PreparedStatement stmt = connection.prepareStatement(
                        "CREATE TABLE gameMetadata (" +
                            "gameName VARCHAR(255) PRIMARY KEY, " +
                            "isLowerBetter BOOLEAN " +
                        ")"
                    )
                ) {
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error ensuring gameMetadata table exists", e
            );
        }
    } /* ensureTableExists */


    @Override
    public void create(GameMetadata metadata) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO gameMetadata (gameName, isLowerBetter) " +
                "VALUES (?, ?)"
            )
        ) {
            stmt.setString(1, metadata.getGameName());
            stmt.setBoolean(2, metadata.isLowerBetter());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error storing game metadata.", e
            );
        }
    } /* create */


    @Override
    public void update(GameMetadata metadata) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE gameMetadata " +
                "SET isLowerBetter = ? " +
                "WHERE gameName = ?"
            )
        ) {
            stmt.setBoolean(1, metadata.isLowerBetter());
            stmt.setString(2, metadata.getGameName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error updating game metadata for game: " + metadata.getGameName(), e
            );
        }
    } /* update */


    @Override
    public GameMetadata retrieveOne(Object key) {
        String gameName = (String) key;
        GameMetadata metadata = null;
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT gameName, isLowerBetter " +
                "FROM gameMetadata " +
                "WHERE gameName = ?"
            )
        ) {
            stmt.setString(1, gameName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                metadata = new GameMetadata(
                    resultSet.getString("gameName"),
                    resultSet.getBoolean("isLowerBetter")
                );
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error retrieving game metadata for game: " + gameName + 
                ". SQL State: " + e.getSQLState(), e
            );
        }
        return metadata;
    } /* retrieveOne */


    @Override
    public List<GameMetadata> retrieveMany(Object criteria) {
        // For this example, we retrieve all. Adjust accordingly if criteria-based retrieval is needed.
        throw new UnsupportedOperationException(
            "retrieveMany method is not supported. Use retrieveAll instead.\n" +
            "For further inquiries, contact the maintainers."
        );
    } /* retrieveMany */


    public List<GameMetadata> retrieveAll() {
        List<GameMetadata> metadataList = new ArrayList<>();
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT gameName, isLowerBetter " +
                "FROM gameMetadata"
            )
        ) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                metadataList.add(new GameMetadata(
                    resultSet.getString("gameName"),
                    resultSet.getBoolean("isLowerBetter")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error retrieving all game metadata.", e
            );
        }
        return metadataList;
    } /* retrieveAll */


    @Override
    public void delete(GameMetadata metadata) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM gameMetadata " +
                "WHERE gameName = ?"
            )
        ) {
            stmt.setString(1, metadata.getGameName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error deleting game metadata for game: " + metadata.getGameName(), e
            );
        }
    } /* delete */
}
