package minigames.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import minigames.server.database.DerbyDatabase;
import minigames.server.highscore.*;
import minigames.server.muddle.MuddleServer;
import io.vertx.core.Launcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class for starting up the game server.
 *
 * This is shaped a little by things that Vertx needs to start itself up
 */
public class Main extends AbstractVerticle {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Port to start the server on.
     * This is a little hacky, but when we run the server, we just put the port argument into this static field
     * so that the GameServer's start method can pick it up later
     */
    public static int port = 8080;

    /**
     * The games that are available for each client. Static so that game servers and Main gan register them
     * without needing to worry about whether the server has started yet.
     */
    public static final GameRegistry gameRegistry = new GameRegistry();

    /**
     * Represents the application's (current) primary database, utilising the Derby embedded database system.
     * This instance provides pooled connections and should be initialised during application startup.
     * Resources and connections associated with this database will be released during application shutdown.
     */
    public static DerbyDatabase derbyDatabase;

    /**
     * Provides a set of APIs to manage and retrieve high scores from the database.
     * This instance interacts directly with the DerbyDatabase to execute database operations.
     * High scores from all game components can be accessed via this API.
     */
    public static HighScoreAPI highScoreAPI;

    /**
     * A place for groups to put code that registers their GameServer with the GameRegistry, etc.
     */
    private static void doWiring() {
        // Register our first demo game
        gameRegistry.registerGameServer("Muddle", new MuddleServer());

        // Initialize the HighScoreAPI
        HighScoreStorage highScoreStorage = new StubHighScoreStorage();
        HighScoreManager highScoreManager = new HighScoreManager(highScoreStorage);
        GlobalLeaderboard globalLeaderboard = new GlobalLeaderboard(highScoreStorage);
        highScoreAPI = new HighScoreAPI(highScoreManager, globalLeaderboard);

        try {
            // Initialise the Derby database, making it accessible for other components.
            derbyDatabase = new DerbyDatabase();
            // It's essential to release all database connections and shut down the database
            // properly before the application terminates to prevent potential resource leaks 
            // or database corruption.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (derbyDatabase != null) {
                    derbyDatabase.shutdown();
                }
            }));
        } catch (Exception e) {
            logger.error("Failed to initialise or interact with the database.", e);
        }

        // Initialise the HighScoreAPI with the Derby database as its backend storage.
        highScoreAPI = new HighScoreAPI(derbyDatabase);

        //adding some dummy/default names to the player list
        players.add("James");
        players.add("Sarah");
        players.add("Andrew");
        players.add("Amy");
        players.add("Robert");
        players.add("Georgia");
    }

    public static void main(String... args) {
        if (args.length > 0) {
            try {
                int p = Integer.parseInt(args[0]);
                if (p >= 1024 && p <= 49151) {
                    port = p;
                } else {
                    logger.error("Port {} is outside the user port range of 1024 to 49151", args[0]);
                }
            } catch (NumberFormatException ex) {
                logger.error("Port {} could not be parsed as a number", args[0]);
            }
        }

        // Register games and services
        doWiring();

        // Ask the Vertx launcher to launch our "Verticle".
        // This will cause Vert.x to start itself up, and then create a Main object and call our Main::start method
        logger.info("About to launch the server");
        Launcher.executeCommand("run", "minigames.server.Main");
    }

    MinigameNetworkServer gameServer;

    /**
     * The start method is called by vertx to initialise this Verticle.
     */
    @Override
    public void start(Promise<Void> promise) {
        logger.info("Our Verticle is being started by Vert.x");
        gameServer = new MinigameNetworkServer(vertx);
        gameServer.start(port);
    }


}