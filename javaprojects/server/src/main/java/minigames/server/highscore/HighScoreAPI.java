package minigames.server.highscore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Map;
import minigames.server.database.DerbyDatabase;


/**
 * Main class
 * 
 * API for game servers to interact with the high score system.
 */
public class HighScoreAPI {

    private HighScoreStorage storage;
    private HighScoreManager manager;
    private GlobalLeaderboard globalLeaderboard;
    private static final Logger logger = LogManager.getLogger(HighScoreAPI.class);

    /**
     * Constructor
     */
    public HighScoreAPI(DerbyDatabase database) {
        this.storage = new DerbyHighScoreStorage(database);
        this.manager = new HighScoreManager(this.storage);
        this.globalLeaderboard = new GlobalLeaderboard(this.storage);
    }

    /**
     * Constructor
     * @param manager The high score manager
     * @param globalLeaderboard The global leaderboard computation mechanism
     */
    public HighScoreAPI(HighScoreManager manager, GlobalLeaderboard globalLeaderboard) {
        this.manager = manager;
        this.globalLeaderboard = globalLeaderboard;
    }

    // Record a new score
    public void recordScore(String playerId, String gameName, int score) {
        try {
            manager.recordScore(playerId, gameName, score);
        } catch (HighScoreException ex) {
            logger.error("Failed to record score for player {} in game {}: {}",
                playerId, gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }

    // Retrieve list of top scores for a game
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        try {
            return manager.getTopScores(gameName, limit);
        } catch (HighScoreException ex) {
            logger.error("Failed to retrieve top scores for game {}: {}",
                gameName, ex.getMessage());
            throw ex;  // Rethrow the exception so that the caller is aware of the failure.
        }
    }

    // Retrieve the personal best score of a player for a game
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
     * Retrieve the global leaderboard.
     * @return A sorted map of player IDs to global scores.
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