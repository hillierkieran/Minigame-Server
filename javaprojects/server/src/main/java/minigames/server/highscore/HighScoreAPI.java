package minigames.server.highscore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Map;
import minigames.server.database.DerbyDatabase;


/**
 * The HighScoreAPI provides a high-level interface for game servers to interact 
 * with the high score system. It allows game servers to record and retrieve 
 * high scores, both for individual games and across all games in the system.
 * 
 * <p>
 * This API acts as an abstraction over the underlying storage mechanism, 
 * which can be a database like the Derby database, or any other storage system.
 * The current implementation of this API uses the Derby database as its storage,
 * but it can be extended to use other storage systems in the future if needed.
 * </p>
 * 
 * <p>
 * Example usage:
 * <pre>
 *     HighScoreAPI api = new HighScoreAPI(new DerbyDatabase());
 *     api.recordScore("player123", "GameA", 100);
 *     List<ScoreRecord> topScores = api.getTopScores("GameA", 10);
 * </pre>
 * </p>
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class HighScoreAPI {

    private HighScoreStorage storage;               // Persistent data storage
    private HighScoreManager manager;               // HighScoreAPI logic
    private GlobalLeaderboard globalLeaderboard;    // Global leaderboard logic
    private static final Logger logger = LogManager.getLogger(HighScoreAPI.class);

    /**
     * Constructs a new HighScoreAPI using the specified DerbyDatabase as the storage mechanism.
     *
     * @param database The DerbyDatabase used for storing and retrieving scores.
     */
    public HighScoreAPI(DerbyDatabase database) {
        this.storage = new DerbyHighScoreStorage(database);
        this.manager = new HighScoreManager(this.storage);
        this.globalLeaderboard = new GlobalLeaderboard(this.storage);
    }

    /**
     * Constructs a new HighScoreAPI using the specified manager and global leaderboard.
     *
     * @param manager The high score manager used for managing scores.
     * @param globalLeaderboard The global leaderboard computation mechanism.
     * 
     * @deprecated This constructor will be removed in future updates. 
     * Use {@link #HighScoreAPI(DerbyDatabase)} instead
     */
    @Deprecated
    public HighScoreAPI(HighScoreManager manager, GlobalLeaderboard globalLeaderboard) {
        this.manager = manager;
        this.globalLeaderboard = globalLeaderboard;
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
     * Retrieves a list of the top scores for a specific game, limited to a specified number.
     *
     * @param gameName The name of the game.
     * @param limit The maximum number of top scores to retrieve.
     * @return A list of the top scores for the game, sorted in descending order.
     * @throws HighScoreException If an error occurs while retrieving the scores.
     */
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        try {
            return manager.getTopScores(gameName, limit);
        } catch (HighScoreException ex) {
            logger.error("Failed to retrieve top scores for game {}: {}",
                gameName, ex.getMessage());
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