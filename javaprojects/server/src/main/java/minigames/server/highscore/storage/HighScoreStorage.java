package minigames.server.highscore;

import java.util.List;

/**
 * HighScoreStorage Interface.
 * <p>
 * Defines methods that any storage mechanism must implement to store and get high scores.
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


    public void registerGame(String gameName, Boolean isLowerBetter);


    public boolean isGameRegistered(String gameName);


    /**
     * Stores a new score record into the storage system.
     * 
     * @param record The {@link ScoreRecord} object representing the player's score for a game.
     * @throws HighScoreException if there's any error during the storage operation.
     */
    public void storeScore(String playerId, String gameName, int score);


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
    public ScoreRecord getScore(String playerId, String gameName);


    /**
     * Retrieves a list of top scores for a specific game.
     * 
     * @param gameName Name of the game for which top scores are needed.
     * @return List of {@link ScoreRecord} objects sorted by scores in descending order.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    public List<ScoreRecord> getHighScores(String gameName);


    /**
     * Retrieves all scores across all games stored in the system.
     * 
     * @return List of {@link ScoreRecord} objects representing scores for all players across all games.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    public List<ScoreRecord> getAllScores();


    /**
     * Retrieves metadata for a specific game. Metadata includes details like whether 
     * a lower score is better for the game or higher.
     * 
     * @param gameName Name of the game for which metadata is needed.
     * @return A {@link GameRecord} object with details about the game's scoring system.
     * @throws HighScoreException if there's any error during the retrieval operation.
     */
    public GameRecord getGame(String gameName);


    /**
     * Deletes a given score record from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteScore(String playerId, String gameName);


    /**
     * Deletes a given game record and all it's scores from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteGame(String gameName);
}