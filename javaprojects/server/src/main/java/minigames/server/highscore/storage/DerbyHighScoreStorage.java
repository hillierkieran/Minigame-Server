package minigames.server.highscore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import minigames.server.database.DatabaseTable;
import minigames.server.database.DatabaseShutdownException;
import minigames.server.database.DerbyDatabase;

import minigames.server.highscore.GameRecord;
import minigames.server.highscore.ScoreRecord;


/**
 * An implementation of the HighScoreStorage interface using the Derby Database.
 * <p>
 * This class provides functionality to interact with the Derby Database system for storing
 * and retrieving game high scores. The implementation leverages Java's JDBC API to manage
 * database connections, perform CRUD operations, and retrieve data.
 * </p>
 * Example usage can be found in the {@link HighScoreAPI} class.
 */
public class DerbyHighScoreStorage implements HighScoreStorage {

    private DerbyDatabase database;
    private GameTable gameTable;
    private ScoreTable scoreTable;


    /**
     * Constructs a new DerbyHighScoreStorage object.
     * 
     * @param database The DerbyDatabase instance to be used for database operations.
     */
    public DerbyHighScoreStorage(DerbyDatabase database) {
        if (!(database instanceof DerbyDatabase)) {
            throw new IllegalArgumentException(
                "Provided database is not an instance of DerbyDatabase"
            );
        }
        this.database = database;
        gameTable = new GameTable(this.database);
        scoreTable = new ScoreTable(this.database, gameTable);
    }


    /**
     * TESTING ONLY
     * Constructs a new DerbyHighScoreStorage object.
     * 
     * @param database The DerbyDatabase instance to be used for database operations.
     */
    DerbyHighScoreStorage(DatabaseTable gameTable, DatabaseTable scoreTable) {
        this.gameTable  = (GameTable)  gameTable;
        this.scoreTable = (ScoreTable) scoreTable;
    }


    @Override
    public void registerGame(String gameName, Boolean isLowerBetter) {
        GameRecord prevRecord = getGame(gameName);
        GameRecord gameRecord = new GameRecord(gameName, isLowerBetter);
        if (prevRecord == null) {
            gameTable.create(gameRecord);
        } else if (Boolean.compare(prevRecord.isLowerBetter(), isLowerBetter) != 0) {
            gameTable.update(gameRecord);
        }
    }


    @Override
    public boolean isGameRegistered(String gameName) {
        return getGame(gameName) != null;
    }


    /**
     * Stores a score record in the database.
     * 
     * @param scoreRecord The score record to be stored.
     */
    @Override
    public void storeScore(String playerId, String gameName, int score) {
        ScoreRecord prevRecord = getScore(playerId, gameName);
        ScoreRecord scoreRecord = new ScoreRecord(playerId, gameName, score);
        if (prevRecord == null) {
            scoreTable.create(scoreRecord);
        } else if (prevRecord.getScore() != score) {
            scoreTable.update(scoreRecord);
        }
    }


    /**
     * Retrieves a list of top scores for a given game
     * 
     * @param gameName The name of the game to retrieve scores for.
     * @return A list of ScoreRecord objects representing the top scores.
     */
    @Override
    public List<ScoreRecord> getHighScores(String gameName) {
        return scoreTable.retrieveMany(gameName);
    }


    /**
     * Retrieves the best score for a given player and game.
     * 
     * @param playerId The ID of the player to retrieve the score for.
     * @param gameName The name of the game to retrieve the score for.
     * @return A ScoreRecord object representing the player's best score.
     */
    @Override
    public ScoreRecord getScore(String playerId, String gameName) {
        return scoreTable.retrieveOne(new ScoreRecord(playerId, gameName, 0));
    }


    /**
     * Retrieves all scores across all games from the database.
     * 
     * @return A list of ScoreRecord objects representing all scores.
     */
    @Override
    public List<ScoreRecord> getAllScores() {
        return scoreTable.retrieveAll();
    }


    /**
     * Retrieves the metadata for a given game from the database.
     * 
     * @param gameName The name of the game to retrieve metadata for.
     * @return A GameRecord object representing the game's metadata.
     */
    @Override
    public GameRecord getGame(String gameName) {
        return gameTable.retrieveOne(new GameRecord(gameName, false));
    }


    /**
     * Deletes a given score record from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    @Override
    public void deleteScore(String playerId, String gameName) {
        scoreTable.delete(new ScoreRecord(playerId, gameName, 0));
    }


    /**
     * Deletes a given game record and all it's scores from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    @Override
    public void deleteGame(String gameName) {
        gameTable.delete(new GameRecord(gameName, false));
    }
}
