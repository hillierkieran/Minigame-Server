package minigames.server.database;


/**
 * This exception is thrown when there is an issue during database initialisation.
 */
public class DatabaseInitialisationException extends DatabaseException {


    /**
     * Constructs a new DatabaseInitialisationException with the specified message.
     *
     * @param message the detail message.
     */
    public DatabaseInitialisationException(String message) {
        super(message);
    }


    /**
     * Constructs a new DatabaseInitialisationException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public DatabaseInitialisationException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a new DatabaseInitialisationException with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public DatabaseInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
