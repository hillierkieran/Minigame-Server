package minigames.server.highscore;

/**
 * Represents a score record object for a player in a particular game.
 */
public class ScoreRecord {
    private String playerId;        // The ID of the player
    private String gameName;        // The name of the game
    private int score;              // The score achieved by the player
    private boolean isLowerBetter;  // Indicates if a lower score is better for this game.

    // ... Constructor, Getters, Setters ...
}