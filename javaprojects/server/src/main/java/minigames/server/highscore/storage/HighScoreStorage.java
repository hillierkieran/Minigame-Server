package minigames.server.highscore;

import java.util.List;

/**
 * Defines methods for high score storage.
 * Implementations may vary (e.g. database, file, etc).
 */
public interface HighScoreStorage {


    /** Registers a new game with scoring preference. */
    public void registerGame(String gameName, Boolean isLowerBetter);


    /** Checks if a game is already registered. */
    public boolean isGameRegistered(String gameName);


    /** Stores a player's score for a game. */
    void storeScore(String playerId, String gameName, int score);


    /** Gets a player's best score for a game. */
    ScoreRecord getScore(String playerId, String gameName);


    /** Retrieves top scores for a game. */
    List<ScoreRecord> getHighScores(String gameName);


    /** Retrieves all scores across all games. */
    List<ScoreRecord> getAllScores();


    /** Gets game metadata (e.g., scoring preference). */
    GameRecord getGame(String gameName);


    /** Deletes a player's score for a game. */
    void deleteScore(String playerId, String gameName);


    /** Deletes a game and all associated scores. */
    void deleteGame(String gameName);


    /** Backup game metadata. */
    void backupGame();


    /** Backup scores. */
    void backupScores();
}