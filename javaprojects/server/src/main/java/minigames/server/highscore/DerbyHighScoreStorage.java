package minigames.server.highscore;

import minigames.server.database.DerbyDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DerbyHighScoreStorage implements HighScoreStorage {

    private DerbyDatabase database;

    /**
     * Constructor
     */
    public DerbyHighScoreStorage(DerbyDatabase database) {
        this.database = database;
    }

    /**
     * Stores a new score-record
     */
    @Override
    public void storeScore(ScoreRecord record) {
        // Using a try-with-resources ensures resources close when block exits (after both success or catch)
        try (
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL insert statement to add the score record
            PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO scores (playerId, gameName, score) VALUES (?, ?, ?)"
            )
        ) {
            // Set the parameters based on the given score record
            preparedStatement.setString(1, record.getPlayerId());
            preparedStatement.setString(2, record.getGameName());
            preparedStatement.setInt(3, record.getScore());

            // Execute the statement to insert the record into the database
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseAccessException("Error storing score.", e);
        }
    }

    /** 
     * Retrieves top n scores for a game of `limit` 
     */
    @Override
    public List<ScoreRecord> retrieveTopScores(String gameName, int limit) {
        List<ScoreRecord> topScores = new ArrayList<>();
        try (
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve top scores
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT playerId, score FROM scores WHERE gameName = ? ORDER BY score DESC LIMIT ?"
            )
        ) {
            // Set the parameters based on the given game and limit
            preparedStatement.setString(1, gameName);
            preparedStatement.setInt(2, limit);

            // Execute the statement and get the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Convert each row in the result set to a ScoreRecord and add to the list
            while (resultSet.next()) {
                topScores.add(new ScoreRecord(
                        resultSet.getString("playerId"),
                        gameName,
                        resultSet.getInt("score")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Error retrieving top scores.", e);
        }
        return topScores;
    }

    /**
     * Retrieves the highest score of a player for a game
     */
    @Override
    public ScoreRecord retrievePersonalBest(String playerId, String gameName) {
        ScoreRecord personalBest = null;
        try (
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve the score of a player
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT score FROM scores WHERE playerId = ? AND gameName = ? ORDER BY score DESC LIMIT 1"
            )
        ) {
            // Set the parameters based on the given player and game
            preparedStatement.setString(1, playerId);
            preparedStatement.setString(2, gameName);

            // Execute the statement and get the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // If there's a result, convert it to a ScoreRecord
            if (resultSet.next()) {
                personalBest = new ScoreRecord(
                        playerId,
                        gameName,
                        resultSet.getInt("score")
                );
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Error retrieving personal best score.", e);
        }
        return personalBest;
    }

    /**
     * Retrieve all scores across all games
     */
    @Override
    public List<ScoreRecord> retrieveAllScores() {
        List<ScoreRecord> allScores = new ArrayList<>();
        try (
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve all scores across all games
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT playerId, gameName, score FROM scores"
            )
        ) {
            // Execute the statement and get the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Convert each row in the result set to a ScoreRecord and add to the list
            while (resultSet.next()) {
                allScores.add(new ScoreRecord(
                        resultSet.getString("playerId"),
                        resultSet.getString("gameName"),
                        resultSet.getInt("score")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Error retrieving all scores.", e);
        }
        return allScores;
    }

    @Override
    public GameMetadata getGameMetadata(String gameName) {
        GameMetadata metadata = null;
        try (
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve all scores across all games
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT gameName, isLowerBetter FROM gameMetadata WHERE gameName = ? LIMIT 1"
            )
        ) {
            // Set the parameter based on the given game
            preparedStatement.setString(1, gameName);

            // Execute the statement and get the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // If there's a result, convert it to a GameMetaData
            if (resultSet.next()) {
                boolean isLowerBetter = resultSet.getBoolean("isLowerBetter");
                metadata = new GameMetadata(gameName, isLowerBetter);
            }

        } catch (SQLException e) {
            throw new DatabaseAccessException("Error retrieving game metadata for game: " + gameName, e);
        }

        return metadata;
    }

    public void close() {
        database.disconnect();
    }
}
