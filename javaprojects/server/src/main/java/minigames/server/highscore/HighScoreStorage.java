package minigames.server.highscore;

import java.util.List;

/**
 * HighScoreStorage Interface.
 * <p>
 * Defines methods that any storage mechanism must implement to store and retrieve high scores.
 * This allows for flexibility in choosing different storage mechanisms while ensuring 
 * a consistent way to interact with the high score data.
 * </p>
 * 
 * <p>
 * Implementations can include but are not limited to database storage, file storage, etc.
 * For a reference implementation using a database, see {@link DerbyHighScoreStorage}.
 * </p>
 */
public interface HighScoreStorage {

    /**
     * Stores a new score record into the storage system.
     * 
     * @param record The {@link ScoreRecord} object representing the player's score for a game.
     * @throws HighScoreException if there's any error during the storage operation.
     */
    void storeScore(ScoreRecord record);

    /**
     * Retrieves a list of top scores for a specific game.
     * 
     * @param gameName Name of the game for which top scores are needed.
     * @param limit The maximum number of top scores to retrieve.
     * @return List of {@link ScoreRecord} objects sorted by scores in descending order.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    List<ScoreRecord> retrieveTopScores(String gameName, int limit);

    /**
     * Retrieves the personal best (highest/lowest score depending on the game's scoring system) 
     * of a player for a specific game.
     * 
     * @param playerId The ID of the player.
     * @param gameName Name of the game for which the personal best is needed.
     * @return A {@link ScoreRecord} object representing the player's best score for the game.
     *         Returns null if no score record found for the player for the game.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    ScoreRecord retrievePersonalBest(String playerId, String gameName);

    /**
     * Retrieves all scores across all games stored in the system.
     * 
     * @return List of {@link ScoreRecord} objects representing scores for all players across all games.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    List<ScoreRecord> retrieveAllScores();

    /**
     * Retrieves metadata for a specific game. Metadata includes details like whether 
     * a lower score is better for the game or higher.
     * 
     * @param gameName Name of the game for which metadata is needed.
     * @return A {@link GameMetadata} object with details about the game's scoring system.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    GameMetadata getGameMetadata(String gameName);
}