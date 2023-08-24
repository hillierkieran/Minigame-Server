package minigames.server.highscore;

import java.util.List;
import java.util.Map;

/**
 * Main class
 * 
 * API for game servers to interact with the high score system.
 */
public class HighScoreAPI {

    private HighScoreStorage storage;
    private HighScoreManager manager;
    private GlobalLeaderboard globalLeaderboard;

    /**
     * Constructor
     */
    public HighScoreAPI() {
        this.storage = new DerbyHighScoreStorage();
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
        manager.recordScore(playerId, gameName, score);
    }

    // Retrieve list of top scores for a game
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        return manager.getTopScores(gameName, limit);
    }

    // Retrieve the personal best score of a player for a game
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        return manager.getPersonalBest(playerId, gameName);
    }

    /**
     * Retrieve the global leaderboard.
     * @return A sorted map of player IDs to global scores.
     */
    public Map<String, Integer> getGlobalLeaderboard() {
        return globalLeaderboard.computeGlobalScores();
    }
}