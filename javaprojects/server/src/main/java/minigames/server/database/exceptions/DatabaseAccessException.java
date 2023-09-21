package minigames.server.database;


/**
 * This exception is thrown when there is an access issue with the database.
 */
public class DatabaseAccessException extends DatabaseException {


    /**
     * Constructs a new DatabaseAccessException with the specified message.
     *
     * @param message the detail message.
     */
    public DatabaseAccessException(String message) {
        super(message);
    }


    /**
     * Constructs a new DatabaseAccessException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public DatabaseAccessException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a new DatabaseAccessException with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
