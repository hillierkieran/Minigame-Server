package minigames.server.highscore;

import java.util.List;

/**
 * Manages the high score functionalities which includes storing and retrieving of high scores 
 * from the HighScoreStorage implementation.
 * <p>
 * This manager is responsible for the logic related to the high score system, such as determining 
 * whether a new score should be recorded or not.
 * </p>
 */
public class HighScoreManager {

    private HighScoreStorage storage;

    /**
     * Constructor to create a new instance of the HighScoreManager.
     *
     * @param storage The HighScoreStorage implementation that this manager should use.
     */
    public HighScoreManager(HighScoreStorage storage) {
        this.storage = storage;
    }

    /**
     * Records a new score if it's better than the player's previous best, considering the game's metadata.
     * 
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @param score The score achieved by the player.
     * @throws HighScoreException If game metadata is not found or any other error occurs.
     */
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

    /**
     * Retrieves a list of the top scores for a specific game, up to a specified limit.
     * 
     * @param gameName The name of the game.
     * @param limit The maximum number of top scores to return.
     * @return A list of ScoreRecord objects containing the top scores for the game.
     */
    public List<ScoreRecord> getTopScores(String gameName, int limit) {
        return storage.retrieveTopScores(gameName, limit);
    }

    /**
     * Retrieves the personal best score of a specific player for a specific game.
     * 
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @return A ScoreRecord object containing the personal best score of the player for the game.
     */
    public ScoreRecord getPersonalBest(String playerId, String gameName) {
        return storage.retrievePersonalBest(playerId, gameName);
    }
}
