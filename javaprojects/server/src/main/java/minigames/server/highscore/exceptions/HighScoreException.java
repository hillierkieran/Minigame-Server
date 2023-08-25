package minigames.server.highscore;

/**
 * Base exception class for all high score related exceptions.
 * 
 * This class represents any form of error or exception 
 * encountered when interacting with the high score system.
 * It provides a generic catch for high score related errors, 
 * making it easier to catch and handle such errors in the calling code.
 * 
 * <p>
 * Example usage:
 * <pre>
 * try {
 *     // some high score operation
 * } catch (HighScoreException ex) {
 *     // handle or log the error
 * }
 * </pre>
 * </p>
 * 
 * @see DatabaseAccessException
 */
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
     * Constructs a new high score exception with the specified detail 
     * message and cause.
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
