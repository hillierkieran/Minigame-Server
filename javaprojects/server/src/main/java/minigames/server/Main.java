package minigames.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Launcher;

/**
 * A class for starting up the game server.
 * 
 * This is shaped a little by things that Vertx needs to start itself up
 */
public class Main extends AbstractVerticle {

    public static void main(String... args) {
        // Ask the Vertx launcher to launch our "Verticle".
        // This will cause Vert.x to start itself up, and then create a Main object and call our Main::start method
        Launcher.executeCommand("run", "minigames.server.Main");
    }

    GameServer gameServer;

    /**
     * The start method is called by vertx to initialise this Verticle.
     */
    @Override
    public void start(Promise<Void> promise) {
        System.out.println("Creating game server");
        gameServer = new GameServer(vertx);
    }


}