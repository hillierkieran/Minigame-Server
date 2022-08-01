package minigames.client;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;
import minigames.rendering.GameMetadata;
import minigames.rendering.GameServerDetails;
import minigames.rendering.NativeCommands;
import minigames.rendering.RenderingPackage;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Optional;

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

    Optional<GameClient> gameClient;

    public MinigameNetworkClient(Vertx vertx) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.gameClient = Optional.empty();

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
    public Future<String> ping() {
        return webClient.get(port, host, "/ping")
            .send()
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            }).map((resp) -> resp.bodyAsString());        
    }

    /** Get the list of GameServers that are supported for this client type */
    public Future<List<GameServerDetails>> getGameServers() {
        return webClient.get(port, host, "/gameServers/Swing")
            .send()
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .map((resp) -> 
                resp.bodyAsJsonArray()
                  .stream()
                  .map((j) -> ((JsonObject)j).mapTo(GameServerDetails.class))
                  .toList()
            )
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }

    /** Get the metadata for all games currently running for a particular gameServer */
    public Future<List<GameMetadata>> getGameMetadata(String gameServer) {
        return webClient.get(port, host, "/games/" + gameServer)
            .send()
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .map((resp) -> 
                resp.bodyAsJsonArray()
                  .stream()
                  .map((j) -> ((JsonObject)j).mapTo(GameMetadata.class))
                  .toList()
            )
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }


    /** Get the metadata for all games currently running for a particular gameServer */
    public Future<RenderingPackage> newGame(String gameServer, String playerName) {
        return webClient.post(port, host, "/newGame/" + gameServer)
            .sendBuffer(Buffer.buffer(playerName))
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .map((resp) -> {
                JsonObject rpj = resp.bodyAsJsonObject();
                return RenderingPackage.fromJson(rpj);
            })
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }


    <T> Optional<T> tryParsing(JsonObject json, Class<T> clazz) {
        try {
            return Optional.of(json.mapTo(clazz));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }


}
