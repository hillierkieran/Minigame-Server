package minigames.server.highscore;

public class HighScoreException extends RuntimeException {
    public HighScoreException(String message) {
        super(message);
    }

    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}