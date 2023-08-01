package minigames.scalafxclient

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import org.apache.logging.log4j.LogManager
import minigames.commands.*
import io.vertx.core.json.JsonObject

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.`type`.TypeReference
import minigames.rendering.GameMetadata
import io.vertx.core.buffer.Buffer
import com.fasterxml.jackson.module.scala.ClassTagExtensions

/** Mapper for Jackson Databind for Scala case classes */
val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build() :: ClassTagExtensions

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
            (resp) => mapper.readValue(resp.bodyAsString(), new TypeReference[Seq[GameServerDetails]] {})
        )
        .onFailure((err) => logger.error(err))

    /** Gets the metadata of currently active games for a game server */
    def getGameMetadata(gameServer:String) = webClient
        .get(port, host, s"/games/$gameServer")
        .send()
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => mapper.readValue(resp.bodyAsString(), new TypeReference[Seq[GameMetadata]] {})
        )
        .onFailure((err) => logger.error(err))

    /** Creates a new game */
    def newGame(gameServer:String, playerName:String) = webClient
        .post(port, host,s"/newGame/$gameServer")
        .sendBuffer(Buffer.buffer(playerName))
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => mapper.readValue(resp.bodyAsString(), new TypeReference[RenderingPackage] {})
        )
        .onFailure((err) => logger.error(err))

    /** Joins a game */
    def joinGame(gameServer:String, game:String, playerName:String) = webClient
        .post(port, host,s"/joinGame/$gameServer/$game")
        .sendBuffer(Buffer.buffer(playerName))
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => mapper.readValue(resp.bodyAsString(), new TypeReference[RenderingPackage] {})
        )
        .onFailure((err) => logger.error(err))

    def send(cp:CommandPackage) = webClient
        .post(port, host, "/command")
        .sendJson(mapper.writer.forType(new TypeReference[CommandPackage] {}).writeValueAsString(cp))
        .onSuccess((resp) => logger.info(resp.bodyAsString()))
        .map(
            (resp) => mapper.readValue(resp.bodyAsString(), new TypeReference[RenderingPackage] {})
            // TODO: Run the rendering package
        )
        .onFailure((err) => logger.error(err))

}