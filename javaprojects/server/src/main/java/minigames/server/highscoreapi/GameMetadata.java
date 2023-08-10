package minigames.server.highscore;

public class GameMetadata {
    private String gameName;
    private boolean isLowerBetter;

    public GameMetadata(String gameName, boolean isLowerBetter) {
        this.gameName = gameName;
        this.isLowerBetter = isLowerBetter;
    }

    // Getters and Setters
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public boolean isLowerBetter() {
        return isLowerBetter;
    }

    public void setLowerBetter(boolean isLowerBetter) {
        this.isLowerBetter = isLowerBetter;
    }
}