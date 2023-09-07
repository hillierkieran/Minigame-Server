package minigames.server.highscore;

import minigames.server.database.DatabaseTable;
import minigames.server.database.DerbyDatabase;
import minigames.server.highscore.GameMetadata;
import minigames.server.highscore.ScoreRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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
    private DatabaseTable games, scores;


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
        games = new GameTable(this.database);
        scores = new ScoreTable(this.database);
    }


    @Override
    public void registerGame(String gameName, Boolean isLowerBetter) {
        GameMetadata game = new GameMetadata(gameName, isLowerBetter);
        GameMetadata prev = (GameMetadata) games.retrieveOne((Object) game);
        if (prev == null) {
            games.create(game);
        } else if (Boolean.compare(prev.isLowerBetter(), isLowerBetter) != 0) {
            games.update(game);
        }
    }


    /**
     * Stores a score record in the database.
     * 
     * @param record The score record to be stored.
     */
    @Override
    public void storeScore(ScoreRecord record) {
        ScoreRecord prev = (ScoreRecord) scores.retrieveOne((Object) record);
        if (prev == null) {
            scores.create(record);
        } else if (prev.getScore() != record.getScore()) {
            scores.update(record);
        }
    }


    /**
     * Retrieves a list of top scores for a given game
     * 
     * @param gameName The name of the game to retrieve scores for.
     * @return A list of ScoreRecord objects representing the top scores.
     */
    @Override
    public List<ScoreRecord> retrieveTopScores(String gameName) {
        return scores.retrieveMany((Object) gameName);
    }


    /**
     * Retrieves the best score for a given player and game.
     * 
     * @param playerId The ID of the player to retrieve the score for.
     * @param gameName The name of the game to retrieve the score for.
     * @return A ScoreRecord object representing the player's best score.
     */
    @Override
    public ScoreRecord retrievePersonalBest(String playerId, String gameName) {
        return (ScoreRecord) scores
            .retrieveOne((Object) new ScoreRecord(playerId, gameName, 0));
    }


    /**
     * Retrieves all scores across all games from the database.
     * 
     * @return A list of ScoreRecord objects representing all scores.
     */
    @Override
    public List<ScoreRecord> retrieveAllScores() {
        return scores.retrieveAll();
    }


    /**
     * Retrieves the metadata for a given game from the database.
     * 
     * @param gameName The name of the game to retrieve metadata for.
     * @return A GameMetadata object representing the game's metadata.
     */
    @Override
    public GameMetadata getGameMetadata(String gameName) {
        return (GameMetadata) games.retrieveOne((Object) gameName);
    }


    /**
     * Closes and disconnects the database.
     */
    public void close() {
        database.shutdown();
    }
}
