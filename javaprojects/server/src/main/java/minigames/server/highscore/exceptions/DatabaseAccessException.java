package minigames.server.highscore;

/**
 * Custom exception that represents any form of database access error 
 * encountered when interacting with the high score system.
 * 
 * This exception is thrown when there are issues related to database 
 * connection, queries, or other database operations.
 * 
 * <p>
 * Example usage:
 * <pre>
 * try {
 *     // some database operation
 * } catch (SQLException ex) {
 *     throw new DatabaseAccessException("Error accessing the database", ex);
 * }
 * </pre>
 * </p>
 * 
 * @see HighScoreException
 */
public class DatabaseAccessException extends HighScoreException {

    /**
     * Constructs a new database access exception with the specified 
     * detail message and cause.
     * 
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). A null value is 
     *        permitted, and indicates that the cause is nonexistent 
     *        or unknown.
     */
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}