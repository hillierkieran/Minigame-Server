package minigames.server.highscore;

import minigames.server.database.Database;
import minigames.server.database.DatabaseTable;
import minigames.server.database.DatabaseAccessException;
import minigames.server.highscore.ScoreRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


public class ScoreTable implements DatabaseTable<ScoreRecord> {

    private Database database;


    public ScoreTable(Database database) {
        this.database = database;
        ensureTableExists();
    }


    @Override
    public void ensureTableExists() {
        try (
            Connection connection = database.getConnection()
        ) {
            ResultSet resultSet = connection.getMetaData().getTables(
                null, null, "scores", null
            );
            if (!resultSet.next()) {
                // Table does not exist, create it
                try (
                    PreparedStatement stmt = connection.prepareStatement(
                        "CREATE TABLE scores (" +
                            "playerId VARCHAR(255), " +
                            "gameName VARCHAR(255) " +
                                "REFERENCES gameMetadata(gameName) " + // game must exist to record a score for it
                                "ON DELETE CASCADE, " + // If a game is deleted, so will it's scores
                            "score INT, " +
                            "PRIMARY KEY (playerId, gameName)" +
                        ")"
                    )
                ) {
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error ensuring scores table exists", e
            );
        }
    } /* ensureTableExists */


    @Override
    public void create(ScoreRecord record) {
        // NOTE: Using a try-with-resources ensures connection closes when block exits
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO scores (playerId, gameName, score) " +
                "VALUES (?, ?, ?)"
            )
        ) {
            preparedStatement.setString(1, record.getPlayerId());
            preparedStatement.setString(2, record.getGameName());
            preparedStatement.setInt(3, record.getScore());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // Check for foreign key constraint violation.
            // In this case, attempting to set a score for a non-existant game.
            // 23xxx is the SQL state code for integrity constraint violations.
            if (e.getSQLState().startsWith("23")) {
                throw new DatabaseAccessException(
                    "Error: Score insertion failed as the game " + 
                    record.getGameName() + 
                    " does not exist.", e
                );
            } else {
                throw new DatabaseAccessException(
                    "Error storing score.", e
                );
            }
        }
    } /* create */


    @Override
    public void update(ScoreRecord record) {
        // Assuming playerId and gameName together can uniquely identify a score record.
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE scores " +
                "SET score = ? " +
                "WHERE playerId = ? AND gameName = ?"
            )
        ) {
            preparedStatement.setInt(1, record.getScore());
            preparedStatement.setString(2, record.getPlayerId());
            preparedStatement.setString(3, record.getGameName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error updating score for playerId: " + record.getPlayerId(), e
            );
        }
    } /* update */


    /**
     * Retrieves a ScoreRecord based on the playerId and gameName fields of the provided ScoreRecord.
     * The score field of the provided ScoreRecord is ignored.
     *
     * @param key A ScoreRecord containing the playerId and gameName to search for.
     * @return The matching ScoreRecord, or null if no match is found.
     */
    @Override
    public ScoreRecord retrieveOne(Object key) {
        String playerId = ((ScoreRecord) key).getPlayerId();
        String gameName = ((ScoreRecord) key).getGameName();
        ScoreRecord record = null;
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT playerId, gameName, score " +
                "FROM scores " +
                "WHERE playerId = ? AND gameName = ?"
            )
        ) {
            preparedStatement.setString(1, playerId);
            preparedStatement.setString(2, gameName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                record = new ScoreRecord(
                    resultSet.getString(playerId),
                    resultSet.getString(gameName),
                    resultSet.getInt("score")
                );
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error retrieving score for playerId: " + playerId +
                " and gameName: " + gameName, e
            );
        }
        return record;
    } /* retrieveOne */


    @Override
    public List<ScoreRecord> retrieveMany(Object criteria) {
        String gameName = (String) criteria;
        List<ScoreRecord> scores = new ArrayList<>();
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT playerId, gameName, score " +
                "FROM scores " +
                "WHERE gameName = ?"
            )
        ) {
            preparedStatement.setString(1, gameName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                scores.add(new ScoreRecord(
                    resultSet.getString("playerId"),
                    resultSet.getString("gameName"),
                    resultSet.getInt("score")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error retrieving scores for game: " + gameName, e
            );
        }
        return scores;
    } /* retrieveMany */



    public List<ScoreRecord> retrieveAll() {
        List<ScoreRecord> scores = new ArrayList<>();
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT playerId, gameName, score " +
                "FROM scores"
            )
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                scores.add(new ScoreRecord(
                    resultSet.getString("playerId"),
                    resultSet.getString("gameName"),
                    resultSet.getInt("score")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error retrieving all scores.", e
                );
        }
        return scores;
    } /* retrieveAll */


    @Override
    public void delete(ScoreRecord record) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM scores " +
                "WHERE playerId = ? AND gameName = ?"
            )
        ) {
            preparedStatement.setString(1, record.getPlayerId());
            preparedStatement.setString(2, record.getGameName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                "Error deleting score for playerId: " + record.getPlayerId(), e
            );
        }
    } /* delete */
}
