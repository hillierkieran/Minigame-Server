package minigames.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The central cub of the client.
 * 
 * GameClients will be given a reference to this. 
 * From this, they can get the main window, to set up their UI
 * They can get the Animator, to register for ticks
 * They gan get a reference to Vertx, for starting any other verticles they might want (though most won't)
 */
public class MinigameNetworkClient {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(MinigameNetworkClient.class);

    /** 
     * Host to connect to. Updated from Main.
     */
    public static String host = "localhost";

    /**
     * Port to connect to. Updated from Main.
     */
    public static int port = 8080;

    Vertx vertx;
    WebClient webClient;
    MinigameNetworkClientWindow mainWindow;
    Animator animator;

    public MinigameNetworkClient(Vertx vertx) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);

        animator = new Animator();
        vertx.setPeriodic(16, (id) -> animator.tick());

        mainWindow = new MinigameNetworkClientWindow(this);
        mainWindow.show();

        mainWindow.showStarfieldMessage("Minigame Network");
    }

    /** Get a reference to the Vertx instance */
    public Vertx getVerx() {
        return this.vertx;
    }

    /** Get a reference to the main window */
    public MinigameNetworkClientWindow getMainWindow() {
        return this.mainWindow;
    }

    /** Get a reference to the animator */
    public Animator getAnimator() {
        return this.animator;
    }

    /** Sends a ping to the server and logs the response */
    public void ping() {
        webClient.get(port, host, "/ping")
            .send()
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });        
    }


}
