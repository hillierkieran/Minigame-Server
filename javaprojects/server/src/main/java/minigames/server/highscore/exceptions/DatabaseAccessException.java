package minigames.server.highscore;

public class DatabaseAccessException extends HighScoreException {
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}