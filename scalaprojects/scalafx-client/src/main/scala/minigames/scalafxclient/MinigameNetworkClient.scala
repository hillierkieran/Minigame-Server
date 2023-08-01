package minigames.scalafxclient

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import org.apache.logging.log4j.LogManager

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
        ping()

    }

    def ping() = webClient
        .get(port, host, "/ping")
        .send()
        .onSuccess((resp) => logger.info(resp.bodyAsString))
        .onFailure((resp) => logger.error("Failed: {}", resp.getMessage))
        .map(_.bodyAsString())

}