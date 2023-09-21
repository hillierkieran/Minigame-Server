package minigames.server.highscore;


/**
 * This exception is thrown when there is error with the high score api.
 */
public class HighScoreException extends RuntimeException {


    /**
     * Constructs a new HighScoreException with the specified message.
     *
     * @param message the detail message.
     */
    public HighScoreException(String message) {
        super(message);
    }


    /**
     * Constructs a new HighScoreException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public HighScoreException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a new HighScoreException with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
