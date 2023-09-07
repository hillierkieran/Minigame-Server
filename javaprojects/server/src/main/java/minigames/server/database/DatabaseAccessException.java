package minigames.server.database;

import java.sql.SQLException;


public class DatabaseAccessException extends RuntimeException {


    // A brief description of the operation that was being performed
    private String operationDescription;


    public DatabaseAccessException(String operationDescription, SQLException cause) {
        super(buildMessage(operationDescription, cause), cause);
        this.operationDescription = operationDescription;
    }


    private static String buildMessage(String operationDescription, SQLException cause) {
        return String.format(
            "Error during operation: %s. SQL State: %s. Error Code: %d. Message: %s.",
            operationDescription, 
            cause.getSQLState(), 
            cause.getErrorCode(), 
            cause.getMessage()
        );
    }


    public String getOperationDescription() {
        return operationDescription;
    }
}
