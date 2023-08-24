package minigames.server.highscore;

import minigames.server.database.DerbyDatabaseAPI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DerbyHighScoreStorage implements HighScoreStorage {

    private DerbyDatabaseAPI database;

    /**
     * Constructor
     */
    public DerbyHighScoreStorage(DerbyDatabaseAPI database) throws SQLException {
        this.database = database;
    }

    /**
     * Stores a new score-record
     */
    @Override
    public void storeScore(ScoreRecord record) {
        try {
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL insert statement to add the score record
            String sql = "INSERT INTO scores (playerId, gameName, score) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // Set the parameters based on the given score record
            preparedStatement.setString(1, record.getPlayerId());
            preparedStatement.setString(2, record.getGameName());
            preparedStatement.setInt(3, record.getScore());

            // Execute the statement to insert the record into the database
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Retrieves top n scores for a game of `limit` 
     */
    @Override
    public List<ScoreRecord> retrieveTopScores(String gameName, int limit) {
        List<ScoreRecord> topScores = new ArrayList<>();
        try {
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve the top scores
            String sql = "SELECT playerId, score FROM scores WHERE gameName = ? ORDER BY score DESC LIMIT ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
            e.printStackTrace();
        }
        return topScores;
    }

    /**
     * Retrieves the highest score of a player for a game
     */
    @Override
    public ScoreRecord retrievePersonalBest(String playerId, String gameName) {
        ScoreRecord personalBest = null;
        try {
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve the personal best score of a player
            String sql = "SELECT score FROM scores WHERE playerId = ? AND gameName = ? ORDER BY score DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
            e.printStackTrace();
        }
        return personalBest;
    }

    /**
     * Retrieve all scores across all games
     */
    @Override
    public List<ScoreRecord> retrieveAllScores() {
        List<ScoreRecord> allScores = new ArrayList<>();
        try {
            // Get a connection to the database
            Connection connection = database.getConnection();

            // Create an SQL select statement to retrieve all scores across games
            String sql = "SELECT playerId, gameName, score FROM scores";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

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
            e.printStackTrace();
        }
        return allScores;
    }

    @Override
    protected void finalize() throws Throwable {
        // Before the object is garbage collected, close the database connection
        super.finalize();
        database.closeConnection();
    }
}
