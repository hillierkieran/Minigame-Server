package minigames.server.highscore;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import minigames.server.database.Database;
import minigames.server.database.DerbyDatabase;


/**
 * Provides an interface for game servers to interact with the high score system.
 * Abstracts the storage mechanism, currently using Derby database.
 * Supports recording/retrieving scores, both game-specific and system-wide.
 * 
 * Example usage:
 * <pre>
 *     // To connect to the api
 *     HighScoreAPI highscore = new HighScoreAPI();
 *     highscore.registerGame(gameName, (Boolean)areLowerScoresBetter);
 * 
 *     // When game ends
 *     highscore.recordScore(playerName, gameName, score);
 * 
 *     // To get high scores
 *     String highScores = highscore.getHighScoresToString(gameName);
 *     // OR
 *     List<ScoreRecord> highScores = highscore.getHighScores(gameName);
 * </pre>
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class HighScoreAPI {

    private static final Logger logger = LogManager.getLogger(HighScoreAPI.class);

    private HighScoreStorage storage;               // Persistent data storage
    private HighScoreManager manager;               // HighScoreAPI logic
    private GlobalLeaderboard globalLeaderboard;    // Global leaderboard logic


    /**
     * Default constructor using the pre-defined database type.
     */
    public HighScoreAPI() {
        this(DerbyDatabase.getInstance());
    }

    /**
     * FOR TESTING
     * Constructs a new HighScoreAPI using the specified DerbyDatabase as the storage mechanism.
     *
     * @param database The DerbyDatabase used for storing and retrieving scores.
     */
    HighScoreAPI(Database database) {
        if (database instanceof DerbyDatabase) {
            storage = new DerbyHighScoreStorage((DerbyDatabase)database);
        } else {
            throw new IllegalArgumentException(
                "Provided database is not supported"
            );
        }
        manager = new HighScoreManager(storage);
        globalLeaderboard = new GlobalLeaderboard(storage);
    }


    /**
     * FOR TESTING 
     * Constructs a new HighScoreAPI using the specified manager and global leaderboard.
     *
     * @param manager The high score manager used for managing scores.
     * @param globalLeaderboard The global leaderboard computation mechanism.
     */
    HighScoreAPI(HighScoreManager manager, GlobalLeaderboard globalLeaderboard) {
        this.manager = manager;
        this.globalLeaderboard = globalLeaderboard;
    }


    public void registerGame(String gameName, Boolean isLowerBetter) {
        manager.registerGame(gameName, isLowerBetter);
    }


    public boolean isGameRegistered(String gameName) {
        return manager.isGameRegistered(gameName);
    }


    /**
     * Records a new score for a player in a specific game. If the score is better than
     * the player's previous best for the game (based on game metadata), it will be stored.
     *
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @param score The score achieved by the player.
     * @throws HighScoreException If an error occurs while recording the score.
     */
    public void recordScore(String playerId, String gameName, int score) {
        try {
            manager.recordScore(playerId, gameName, score);
        } catch (HighScoreException ex) {
            logger.error("Failed to record score for player {} in game {}: {}",
                playerId, gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }


    /**
     * Retrieves the personal best score of a player for a specific game.
     *
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @return A ScoreRecord representing the player's best score for the game, 
     *         or null if the player has no score for the game.
     * @throws HighScoreException If an error occurs while retrieving the score.
     */
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        try {
            return manager.getPersonalBest(playerId, gameName);
        } catch (HighScoreException ex) {
            logger.error("Failed to retrieve personal best for player {} in game {}: {}",
                playerId, gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }


    // Renamed method to getHighScores
    @Deprecated
    public List<ScoreRecord> getTopScores(String gameName) {
        return getHighScores(gameName);
    }


    /**
     * Retrieves a list of the top scores for a specific game, limited to a specified number.
     *
     * @param gameName The name of the game.
     * @param limit The maximum number of top scores to retrieve.
     * @return A list of the top scores for the game, sorted in descending order.
     * @throws HighScoreException If an error occurs while retrieving the scores.
     */
    public List<ScoreRecord> getHighScores(String gameName) {
        try {
            return manager.getHighScores(gameName);
        } catch (HighScoreException ex) {
            logger.error("Failed to retrieve top scores for game {}: {}",
                gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }


    /**
     * Retrieves string listing the high scores for a specific game.
     * 
     * @param gameName The name of the game.
     * @return A string listing all the high scores for the game.
     */
    public String getHighScoresToString(String gameName) {
        try {
            return manager.getHighScoresToString(gameName);
        } catch (HighScoreException ex) {
            logger.error("Failed to retrieve top scores for game {}: {}",
                gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }


    /**
     * Deletes a given score record from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteScore(String playerId, String gameName) {
        manager.deleteScore(playerId, gameName);
    }


    /**
     * Deletes a given game record and all it's scores from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteGame(String gameName) {
        manager.deleteGame(gameName);
    }


    /**
     * Computes and retrieves the global leaderboard, which ranks players based on 
     * their aggregated performance across all games in the system.
     *
     * @return A sorted map of player IDs to their global scores, with players ranked 
     *         in ascending order of their aggregated performance.
     * @throws HighScoreException If an error occurs while computing the leaderboard.
     */
    public Map<String, Integer> getGlobalLeaderboard() {
        try {
            return globalLeaderboard.computeGlobalScores();
        } catch (HighScoreException ex) {
            logger.error("Failed to compute the global leaderboard: {}",
                ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }
}