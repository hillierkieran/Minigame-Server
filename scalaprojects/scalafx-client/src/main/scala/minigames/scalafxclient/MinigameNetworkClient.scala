package minigames.scalafxclient

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import org.apache.logging.log4j.LogManager
import minigames.commands.*
import io.vertx.core.json.JsonObject

import minigames.rendering.*
import io.vertx.core.buffer.Buffer
import scalafx.scene.control.Label
import scalafx.application.Platform
import scala.util.Success
import scala.util.Failure

/**
 * The central cub of the client.
 * 
 * GameClients will be given a reference to this. 
 * From this, they can get the main window, to set up their UI
 * They can get the Animator, to register for ticks
 * They gan get a reference to Vertx, for starting any other verticles they might want (though most won't)
 */
class MinigameNetworkClient(val host:String = "localhost", val port:Int = 8080) {

    /** A logger for logging output */
    val logger = LogManager.getLogger(MinigameNetworkClient.getClass)

    logger.info(s"Starting network client, connecting to $host:$port")

    val vertx:Vertx = Vertx.vertx()
    vertx.setPeriodic(16, (id) => Animator.tick());

    /** The web client makes server requests for us */
    val webClient = WebClient.create(vertx)

    /** The borderlayout UI */
    val mainWindow = MinigameNetworkClientWindow(this)

    /** The currently showing game client, if any */
    private var gameClient:Option[GameClient] = None

    def runMainMenuSequence():Unit = {
        mainWindow.show()
        mainWindow.showStarfieldMessage("Minigame Network")
        for 
            _ <- ping()
            list <- getGameServers()
        yield
            logger.info(s"Game Servers: $list")
            mainWindow.showGameServers(list)
    }


    def ping() = webClient
        .get(port, host, "/ping")
        .send()
        .onSuccess((resp) => logger.info(resp.bodyAsString))
        .onFailure((resp) => logger.error("Failed: {}", resp.getMessage))
        .map(_.bodyAsString())

    /** Gets the game servers that are available for this client type */
    def getGameServers() = webClient
        .get(port, host, "/gameServers/Scalafx")
        .send()
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            // Jackson's deserialisation works for some built-in types and case classes
            // It's a curious syntax, but it works. 
            (resp) => GameServerDetails.fromJsonArray(resp.bodyAsJsonArray())
        )
        .onFailure((err) => logger.error(err))

    /** Gets the metadata of currently active games for a game server */
    def getGameMetadata(gameServer:String) = webClient
        .get(port, host, s"/games/$gameServer")
        .send()
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => GameMetadata.fromJsonArray(resp.bodyAsJsonArray())
        )
        .onFailure((err) => logger.error(err))

    /** Creates a new game */
    def newGame(gameServer:String, playerName:String) = webClient
        .post(port, host,s"/newGame/$gameServer")
        .sendBuffer(Buffer.buffer(playerName))
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => RenderingPackage.fromJson(resp.bodyAsJsonObject())
        )
        .onFailure((err) => logger.error(err))
        .onSuccess((rp) => runRenderingPackage(rp))

    /** Joins a game */
    def joinGame(gameServer:String, game:String, playerName:String) = webClient
        .post(port, host,s"/joinGame/$gameServer/$game")
        .sendBuffer(Buffer.buffer(playerName))
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => RenderingPackage.fromJson(resp.bodyAsJsonObject())
        )
        .onFailure((err) => logger.error(err))
        .onSuccess((rp) => runRenderingPackage(rp))


    def send(cp:CommandPackage) = {
        val json = cp.toJson
        logger.info("Sending {}", json)

        webClient
            .post(port, host, "/command")
            .sendJson(json)
            .onSuccess((resp) => logger.info(resp.bodyAsString()))
            .map(
                (resp) => RenderingPackage.fromJson(resp.bodyAsJsonObject())
            )
            .onFailure((err) => logger.error(err))
            .onSuccess((rp) => runRenderingPackage(rp))
    }

    /** Runs a "native command" from a rendering package */
    private def executeNative(metadata:GameMetadata, command:NativeCommand):Unit = 
        command match {
            case NativeCommand.QuitToMenu => 
                for gc <- gameClient do gc.closeGame()
                gameClient = None
                runMainMenuSequence()

            case NativeCommand.ShowMenuError(message) => 
                if gameClient.isEmpty then
                    Platform.runLater {
                        mainWindow.clearSouth()
                        mainWindow.addSouth(new Label { text = message })
                    }
                else logger.error("Tried to show a menu error when a game was loaded")

            case NativeCommand.LoadClient(clientName, gameServer, game, player) => 
                logger.info(s"Loading client $command")
                getGameClient(clientName) match {
                    case Some(gc) => 
                        this.gameClient = Some(gc)
                        Platform.runLater { 
                            mainWindow.clearAll() 
                            gc.load(this, metadata, player)
                            App.stage.sizeToScene()
                        }

                    case None => logger.error("No such game client")
                }

        }

    /** Runs a rendering package -- a set of instructions for the client sent by the server */
    private def runRenderingPackage(rp:RenderingPackage) = {
        logger.info("Running rendering package")
        for json <- rp.renderingCommands do 
            // First, see if it was a "native command"
            NativeCommand.tryParsing(json) match {
                case Some(Success(nativeCommand)) =>
                    executeNative(rp.metadata, nativeCommand)
                case Some(Failure(err)) => 
                    logger.error("Failed to parse native command", err)

                case None => 
                    // This is a game command
                    for gc <- gameClient do gc.execute(rp.metadata, json)

            }



    }

}