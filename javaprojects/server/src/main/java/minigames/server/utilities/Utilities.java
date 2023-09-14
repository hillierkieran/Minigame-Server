package minigames.server.utilities;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Utilities class providing various helper functions.
 * 
 * Add methods as needed.
 */
public class Utilities {

    private static final Logger logger = LogManager.getLogger(Utilities.class);
    private static final String SERVER_PROPERTIES = "config.properties";


    /**
     * Returns the name of the direct caller method.
     *
     * @return The name of the caller method or null if unavailable.
     */
    public static String getThisMethodName() {
        return getMethodName(2);
    }


    /**
     * Returns the name of the caller's caller method (grandparent).
     *
     * @return The name of the grandparent method or null if unavailable.
     */
    public static String getCallerMethodName() {
        return getMethodName(3);
    }


    /**
     * Returns the name of the method at the specified index in the current stack trace.
     *
     * @param i The index in the stack trace to retrieve the method name from.
     * @return The name of the method or null if unavailable.
     */
    public static String getMethodName(int i) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        // Check if we have enough depth in the call stack to get the grandparent
        if (i > 1 && stackTraceElements.length > i) {
            return stackTraceElements[i].getMethodName();
        } else {
            return null;
        }
    }


    // Get server properties file
    public static Properties getProperties() {
        return getProperties(SERVER_PROPERTIES);
    }

    // Get properties file
    public static Properties getProperties(String filename) {
        // Attempt to fetch properties file
        InputStream input = Utilities.class.getClassLoader().getResourceAsStream(filename);
        // Check if found
        if (input == null) {
            // Not found. Log the error and throw an exception
            String message = "Unable to find " + filename;
            logger.error(message);
            throw new RuntimeException(message);
        }
        // Found. Load properties from the input stream
        Properties properties = new Properties();
        try (input) {
            properties.load(input); // on error, throws IOException
        } catch (IOException e) {
            String message = "Unable to load " + filename;
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
        return properties;
    }
}
