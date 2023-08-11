package minigames.server.highscore;

import java.util.Objects;

/**
 * Represents a score record object for a player in a particular game.
 */
public class ScoreRecord {
    private String playerId;        // The ID of the player
    private String gameName;        // The name of the game
    private int score;              // The score achieved by the player

    /**
     * Constructor
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @param score The score achieved by the player.
     */
    public ScoreRecord(String playerId, String gameName, int score) {
        this.playerId = playerId;
        this.gameName = gameName;
        this.score = score;
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public String getGameName() { return gameName; }
    public int getScore() { return score; }

    // Setters
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    public void setScore(int score) { this.score = score; }

    // ToString
    @Override
    public String toString() {
        return "ScoreRecord{" +
                "playerId='" + playerId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", score=" + score +
                '}';
    }
}