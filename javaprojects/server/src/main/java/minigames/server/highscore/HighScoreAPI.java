package minigames.server.highscore;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import minigames.server.database.Database;
import minigames.server.database.DerbyDatabase;


/**
 * Main interface for game servers to engage with the high score system.
 * Provides methods for game-specific and system-wide score operations.
 * <p>
 * Usage:
 * <pre>
 *     HighScoreAPI highscore = new HighScoreAPI();
 *     highscore.registerGame("GameName", true);  // For games where lower score is better
 *     highscore.recordScore("playerName", "GameName", 9001);
 *     List<ScoreRecord> topScores = highscore.getHighScores("GameName");
 *     // or
 *     List<ScoreRecord> highScores = highscore.getHighScores("GameName");
 * </pre>
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class HighScoreAPI {

    private static final Logger logger = LogManager.getLogger(HighScoreAPI.class);
    private HighScoreStorage storage;
    private HighScoreManager manager;
    private GlobalLeaderboard globalLeaderboard;


// Constructors

    /** Constructor. */
    public HighScoreAPI() {
        this(DerbyDatabase.getInstance());
    }

    /** Constructor.
     *  @param database Custom database setup for testing. */
    HighScoreAPI(Database database) {
        if (database instanceof DerbyDatabase)
            storage = new DerbyHighScoreStorage((DerbyDatabase)database);
        else
            throw new IllegalArgumentException("Provided database is not supported");
        manager = new HighScoreManager(storage);
        globalLeaderboard = new GlobalLeaderboard(storage);
    }

    /** Constructor.
     *  @param manager Custom manager setup for testing.
     *  @param globalLeaderboard Custom leaderboard setup for testing. */
    HighScoreAPI(HighScoreManager manager, GlobalLeaderboard globalLeaderboard) {
        this.manager = manager;
        this.globalLeaderboard = globalLeaderboard;
    }

// Game registration

    /** 
     * @param gameName Game identifier.
     * @param isLowerBetter True if lower scores are better for this game.
     */
    public void registerGame(String gameName, Boolean isLowerBetter) {
        manager.registerGame(gameName, isLowerBetter);
    }

    /** 
     * @param gameName Game identifier.
     * @return True if the game is already registered.
     */
    public boolean isGameRegistered(String gameName) {
        return manager.isGameRegistered(gameName);
    }

    /** 
     * @param gameName Game identifier.
     */
    public void deleteGame(String gameName) {
        manager.deleteGame(gameName);
    }


// Record scores

    /** 
     * Records a player's score for a game.
     * @param playerId Player identifier.
     * @param gameName Game identifier.
     * @param score Player's score.
     */
    public void recordScore(String playerId, String gameName, int score) {
        try {
            manager.recordScore(playerId, gameName, score);
        } catch (HighScoreException e) {
            logger.error("Failed to record score for player {} in game {}: {}", playerId, gameName, e.getMessage());
            throw e;
        }
    }

    /** 
     * @param playerId Player identifier.
     * @param gameName Game identifier.
     */
    public void deleteScore(String playerId, String gameName) {
        manager.deleteScore(playerId, gameName);
    }


// Get scores for a game

    /** 
     * @param playerId Player identifier.
     * @param gameName Game identifier.
     * @return Player's best score for the game.
     */
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        try {
            return manager.getPersonalBest(playerId, gameName);
        } catch (HighScoreException e) {
            logger.error("Failed to retrieve personal best for player {} in game {}: {}", playerId, gameName, e.getMessage());
            throw e;
        }
    }

    /** 
     * @param gameName Game identifier.
     * @return Top scores for the game.
     */
    public List<ScoreRecord> getHighScores(String gameName) {
        try {
            return manager.getHighScores(gameName);
        } catch (HighScoreException e) {
            logger.error("Failed to retrieve top scores for game {}: {}", gameName, e.getMessage());
            throw e;
        }
    }

    /** 
     * @param gameName Game identifier.
     * @return String representation of top scores for the game.
     */
    public String getHighScoresToString(String gameName) {
        try {
            return manager.getHighScoresToString(gameName);
        } catch (HighScoreException e) {
            logger.error("Failed to retrieve top scores for game {}: {}", gameName, e.getMessage());
            throw e;
        }
    }


// Get global rankings

    /** 
     * @return Players' rankings across all games.
     */
    public Map<String, Integer> getGlobalLeaderboard() {
        try {
            return globalLeaderboard.computeGlobalScores();
        } catch (HighScoreException e) {
            logger.error("Failed to compute the global leaderboard: {}",e.getMessage());
            throw e;
        }
    }


// Deprecated methods, don't use.

    /** @deprecated Use getHighScores instead. */
    @Deprecated
    public List<ScoreRecord> getTopScores(String gameName) {
        return getHighScores(gameName);
    }
}