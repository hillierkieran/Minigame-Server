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

    // Record a new score if it's better than the previous best, considering game metadata
    public void recordScore(String playerId, String gameName, int score) {
        GameMetadata gameMetadata = storage.getGameMetadata(gameName);
        if (gameMetadata == null) {
            throw new HighScoreException("Game metadata not found for game: " + gameName);
        }

        ScoreRecord currentBest = storage.retrievePersonalBest(playerId, gameName);

        if (currentBest == null) {
            storage.storeScore(new ScoreRecord(playerId, gameName, score));
            return;
        }

        boolean shouldRecord;
        if (gameMetadata.isLowerBetter()) {
            shouldRecord = score < currentBest.getScore();
        } else {
            shouldRecord = score > currentBest.getScore();
        }

        if (shouldRecord) {
            storage.storeScore(new ScoreRecord(playerId, gameName, score));
        }
    }

    // Retrieve list of top scores for a game
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        return storage.retrieveTopScores(gameName, limit);
    }

    // Retrieve the personal best score of a player for a game
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        return storage.retrievePersonalBest(playerId, gameName);
    }
}
