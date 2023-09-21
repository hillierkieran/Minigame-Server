package minigames.server.highscore;


/**
 * Represents game metadata for the high score system.
 * Details how scores are interpreted (e.g., if lower scores are better).
 */
public class GameRecord {

    private String gameName;
    private boolean isLowerBetter;


    /**
     * @param gameName Game's name.
     * @param isLowerBetter True if lower scores are better, false otherwise.
     */
    public GameRecord(String gameName, boolean isLowerBetter) {
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