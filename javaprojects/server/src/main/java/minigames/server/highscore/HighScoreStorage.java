package minigames.server.highscore;

import java.util.List;

/**
 * Interface for storage mechanisms to store and retrieve high scores.
 * 
 * TODO: Once the database is decided, we can start to implement this...
 */
public interface HighScoreStorage {

    // Stores a new score record
    void storeScore(ScoreRecord record);

    // Retrieves top scores for a game with a limit
    List<ScoreRecord> retrieveTopScores(String gameName, int limit);

    // Retrieves the highest score of a player for a game
    ScoreRecord retrievePersonalBest(String playerId, String gameName);

    // Retrieve all scores across all games
    List<ScoreRecord> retrieveAllScores();

    // Retrieve a game's metadata
    GameMetadata getGameMetadata(String gameName);
}