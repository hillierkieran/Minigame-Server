package minigames.server.highscore;

import java.util.Objects;


/**
 * Represents a record of a player's score for a particular game.
 * 
 * This class encapsulates the details of a score achieved by a player in a specific game.
 * It includes the player's ID, the name of the game, and the score the player achieved.
 * 
 * For example, if a player named "John" achieved a score of 200 in "GameA", 
 * a ScoreRecord would represent this as:
 * 
 * playerId = "John"
 * gameName = "GameA"
 * score = 200
 * 
 * This class can be used to store, retrieve, and manipulate player score details in the high score system.
 */
public class ScoreRecord {

    private String playerId;    // The unique identifier of the player
    private String gameName;    // The name of the game where the score was achieved
    private int score;          // The numeric score value achieved by the player


    /**
     * Constructs a ScoreRecord object with the provided player ID, game name, and score.
     * 
     * @param playerId The unique identifier of the player.
     * @param gameName The name of the game where the score was achieved.
     * @param score The numeric score value achieved by the player.
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
        return "ScoreRecord{" +
                "playerId='" + playerId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", score=" + score +
                '}';
    }
}