package minigames.server.highscore;

/**
 * Represents the metadata associated with a game in the high score system.
 * <p>
 * The metadata provides details about how scores should be interpreted for a given game.
 * For instance, some games might treat lower scores as better (e.g., golf), while others
 * might treat higher scores as better.
 * </p>
 * <p>
 * This class should be used by developers to understand how to interpret the scores of a game.
 * It also aids in the process of ranking scores for leaderboard generation.
 * </p>
 */
public class GameMetadata {
    private String gameName;        // The name of the game
    private boolean isLowerBetter;  // Flag indicating if a lower score is better for this game

    /**
     * Constructor to initialise a GameMetadata object with its game name and scoring preference.
     *
     * @param gameName The name of the game.
     * @param isLowerBetter A boolean flag indicating if a lower score is better for this game.
     *                      True if a lower score is better, false otherwise.
     */
    public GameMetadata(String gameName, boolean isLowerBetter) {
        this.gameName = gameName;
        this.isLowerBetter = isLowerBetter;
    }

    // Getters
    public String getGameName() { return gameName; }
    public boolean isLowerBetter() { return isLowerBetter; }

    // Setters
    public void setGameName(String gameName) { this.gameName = gameName;}
    public void setLowerBetter(boolean isLowerBetter) { this.isLowerBetter = isLowerBetter; }
}