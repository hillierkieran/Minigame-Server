package minigames.server.highscore;


public class HighScoreException extends RuntimeException {


    /**
     * Constructs a new high score exception with the specified detail message.
     * 
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     */
    public HighScoreException(String message) {
        super(message);
    }


    /**
     * Constructs a new high score exception with the specified detail message and cause.
     * 
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). A null value is 
     *        permitted, and indicates that the cause is nonexistent 
     *        or unknown.
     */
    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
