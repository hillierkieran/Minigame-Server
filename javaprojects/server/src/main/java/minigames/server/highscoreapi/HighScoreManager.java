package minigames.server.highscore;

import java.util.List;

/**
 * Manages the retrieval and storage of high scores 
 * using the HighScoreStorage implementation.
 */
public class HighScoreManager {

    private HighScoreStorage storage;

    public HighScoreManager(HighScoreStorage storage) {
        this.storage = storage;
    }

    // TODO: Implement the methods exposed in HighScoreAPI by using the storage ...

    // Record a new score if better than previous best
    public void recordScore(String playerId, String gameName, int score) {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Retrieve list of top scores for a game
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Retrieve the personal best score of a player for a game
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not yet implemented");
    }

}