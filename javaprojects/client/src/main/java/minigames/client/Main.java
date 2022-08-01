package minigames.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
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

    /** Where GameClients should register themselves in doWiring */
    public static final ClientRegistry clientRegistry = new ClientRegistry();

    MinigameNetworkClient client;

    /**
     * A place for groups to put code that registers their GameClient with the ClientRegistry, etc.
     */
    private static void doWiring() {

    }

    public static void main(String... args) {
        if (args.length > 0) {
            String[] parts = args[0].split(":");
            switch (parts.length) {
                case 1: 
                    MinigameNetworkClient.host = args[0];
                    break;
                case 2:
                    MinigameNetworkClient.host = parts[0];
                    try {
                        MinigameNetworkClient.port = Integer.parseInt(parts[1]);                
                    } catch (NumberFormatException ex) {
                        logger.error("Port {} could not be parsed as a number", args[0]);
                    }
                default:
                    logger.error("Too many : in host string");
            }
        }

        // Register games and services
        doWiring();

        // Ask the Vertx launcher to launch our "Verticle".
        // This will cause Vert.x to start itself up, and then create a Main object and call our Main::start method
        logger.info("About to launch the client");
        Launcher.executeCommand("run", "minigames.client.Main");
    }


    /**
     * The start method is called by vertx to initialise this Verticle.
     */
    @Override
    public void start(Promise<Void> promise) {
        logger.info("Our Verticle is being started by Vert.x");
        client = new MinigameNetworkClient(vertx);
        client.ping().flatMap((s) -> client.getGameServers()).map((list) -> {
            logger.info("Got servers {}", list);
            return list;
        }).onFailure((ex) -> logger.error("Failed {}", ex));
    }


}