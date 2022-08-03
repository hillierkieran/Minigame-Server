package minigames.client;

import java.util.List;
import java.util.Optional;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import minigames.commands.CommandPackage;
import minigames.rendering.GameMetadata;
import minigames.rendering.GameServerDetails;
import minigames.rendering.NativeCommands.LoadClient;
import minigames.rendering.NativeCommands.QuitToMenu;
import minigames.rendering.NativeCommands.ShowMenuError;
import minigames.rendering.RenderingPackage;

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

    /** Creates a new game on the server, running any commands that come back */
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
            .onSuccess((rp) -> runRenderingPackage(rp))
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }

    /** Joins a game on the server, running any commands that come back */
    public Future<RenderingPackage> joinGame(String gameServer, String game, String playerName) {
        return webClient.post(port, host, "/joinGame/" + gameServer + "/" + game)
            .sendBuffer(Buffer.buffer(playerName))
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .map((resp) -> {
                JsonObject rpj = resp.bodyAsJsonObject();
                return RenderingPackage.fromJson(rpj);
            })
            .onSuccess((rp) -> runRenderingPackage(rp))
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }

    /** Sends a CommandPackage to the server, running any commands that come back */
    public Future<RenderingPackage> send(CommandPackage cp) {
        return webClient.post(port, host, "/command")
            .sendJson(cp)
            .onSuccess((resp) -> {
                logger.info(resp.bodyAsString());
            })
            .map((resp) -> {
                JsonObject rpj = resp.bodyAsJsonObject();
                return RenderingPackage.fromJson(rpj);
            })
            .onSuccess((rp) -> runRenderingPackage(rp))
            .onFailure((resp) -> {
                logger.error("Failed: {} ", resp.getMessage());
            });
    }

    /**
     * Runs the sequence that opens the main menu - starting with a title card before communicating with
     * the server to get a list of available games.
     */
    public void runMainMenuSequence() {
        mainWindow.showStarfieldMessage("Minigame Network");
        
        ping().flatMap((s) -> getGameServers()).map((list) -> {
            logger.info("Got servers {}", list);
            return list;
        }).map((l) -> {
            mainWindow.showGameServers(l);
            return l;
        });
    }

    /** Executes a LoadClient command */
    private void execute(GameMetadata metadata, LoadClient lc) {
        logger.info("Loading client {} ", lc);
        mainWindow.clearAll();
        GameClient gc = Main.clientRegistry.getGameClient(lc.clientName());
        gameClient = Optional.of(gc);
        gc.load(this, metadata, lc.player());
    }

    /** Executes the QuitToMenu command */
    private void execute(QuitToMenu qtm) {
        gameClient.ifPresent((gc) -> gc.closeGame());
        gameClient = Optional.empty();

        runMainMenuSequence();
    }

    /** Executes a ShowMenuError command */
    private void execute(ShowMenuError sme) {
        // We can only show an error if there's no game client 
        // (otherwise we might mess with its display)
        if (gameClient.isEmpty()) {
            mainWindow.clearSouth();
            JLabel l = new JLabel(sme.message());
            mainWindow.addSouth(l);
            mainWindow.pack();
        }
    }

    /** 
     * Interprets and runs a rendering command. 
     * If this is one of the (3) known native command, it runs it. 
     * If not, it passes it directly on to the current GameClient to interpret.
     */
    private void interpretCommand(GameMetadata metadata, JsonObject json) {
        logger.info("Interpreting command {}", json);

        // Try the native commands first
        boolean handled = false;

        Optional<LoadClient> olc = LoadClient.tryParsing(json);
        if (olc.isPresent()) {
            handled = true;
            execute(metadata, olc.get());
        }

        Optional<ShowMenuError> osme = ShowMenuError.tryParsing(json);
        if (osme.isPresent()) {
            handled = true;
            execute(osme.get());
        }

        Optional<QuitToMenu> qtm = QuitToMenu.tryParsing(json);
        if (qtm.isPresent()) {
            handled = true;
            execute(qtm.get());
        }

        if (!handled) {
            gameClient.ifPresent((gc) -> gc.execute(metadata, json));
        }
    }

    /** Interprets and executes all the commands in a rendering package */
    private void runRenderingPackage(RenderingPackage rp) {
        logger.info("Running rendering package");
        GameMetadata gm = rp.metadata();

        for (JsonObject json : rp.renderingCommands()) {
            interpretCommand(gm, json);
        }
    }

}
