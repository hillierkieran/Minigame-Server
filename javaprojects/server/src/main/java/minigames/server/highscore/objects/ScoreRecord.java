package minigames.server.highscore;

import java.util.Objects;


/**
 * Represents a player's score record for a game.
 * Contains player's ID, game name, and achieved score.
 */
public class ScoreRecord {

    private String playerId;
    private String gameName;
    private int score;


    /**
     * @param playerId  Player's unique ID.
     * @param gameName  Name of the game.
     * @param score     Achieved score.
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


    /**
     * Provides a string representation of this ScoreRecord object.
     * 
     * @return A string representing the player ID, game name, and score of this record.
     */
    @Override
    public String toString() {
        return "ScoreRecord{" + "playerId=" + playerId + ", gameName='" + gameName + ", score=" + score + '}';
    }
}