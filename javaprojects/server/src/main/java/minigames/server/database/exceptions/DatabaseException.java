package minigames.server.database;

import java.sql.SQLException;


/**
 * This exception is thrown when there is error with the database.
 * It wraps around standard exceptions like SQLException
 * to provide more specific error messages.
 */
public class DatabaseException extends RuntimeException {


    /**
     * Constructs a new DatabaseException with the specified message.
     *
     * @param message the detail message.
     */
    public DatabaseException(String message) {
        super(message);
    }


    /**
     * Constructs a new DatabaseException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a new DatabaseException with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public DatabaseException(String message, Throwable cause) {
        super(
            cause instanceof SQLException
                ? buildMessage(message, (SQLException) cause)
                : message,
            cause
        );
    }


    /**
     * Builds a detailed error message using the given message and SQLException details.
     *
     * @param message the detail message.
     * @param cause the SQLException causing this exception.
     * @return the detailed error message.
     */
    private static String buildMessage(String message, SQLException cause) {
        return String.format(
            "Error during operation: %s. SQL State: %s. Error Code: %d. Message: %s.",
            message, 
            cause.getSQLState(), 
            cause.getErrorCode(), 
            cause.getMessage()
        );
    }
}
