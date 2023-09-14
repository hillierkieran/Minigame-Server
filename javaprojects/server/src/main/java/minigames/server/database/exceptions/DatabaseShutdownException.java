package minigames.server.database;


/**
 * This exception is thrown when there is an issue during database shutdown.
 */
public class DatabaseShutdownException extends Exception {


    /**
     * Constructs a new DatabaseShutdownException with the specified message.
     *
     * @param message the detail message.
     */
    public DatabaseShutdownException(String message) {
        super(message);
    }


    /**
     * Constructs a new DatabaseShutdownException with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public DatabaseShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
