package minigames.scalajsclient

import org.scalajs.dom
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.js
import js.Thenable.Implicits._
import js.JSConverters._

/**
 * A facade type for our GameServerDetails JSON structure.
 * These only come up from the server, so we only need to reference into them not create them.
 * In which case, we can just define a type facade over the json structure.
 * 
 * Note that Scala won't do any checks to make sure the JSON really matches this structure - 
 * we're just informing the Scala compiler that these are the fields that the JSON will have
 */
@js.native
trait GameServerDetails extends js.Object {
    val name:String = js.native
    val description:String = js.native
}

/**
 * Another facade type for an immutable lump of JSON sent from the server
 * 
 * Beware - if we change "name" to more explicitly be "gameName" or "gameId", we'll need to change the name of the field
 */
@js.native
trait GameMetadata extends js.Object {
    val gameServer:String = js.native
    val name:String = js.native
    val players:js.Array[String] = js.native
    val joinable:Boolean = js.native
}

object MinigameNetworkClient {

    // Get the MGN server location from the search string
    val serverRoot = {
        val serverUrl = dom.window.location.search.drop(1)
        if (serverUrl.endsWith("/")) then "http://" + serverUrl else "http://" + serverUrl + "/"
    }
    dom.console.log("MGN server base url", serverRoot)

    /** GET to route /ping */
    def ping():Future[String] = {
        for 
            response <- dom.fetch(serverRoot + "/ping") 
            text <- response.text()
        yield text
    }

    /** GET to /gameServers/Scalajs */
    def getGameServers():Future[Seq[GameServerDetails]] = {
        for
            response <- dom.fetch(serverRoot + "/gameServers/Scalajs") 
            json <- response.json()
        yield 
            json.asInstanceOf[js.Array[GameServerDetails]].toSeq            
    }

    /** GET to /games/:game */
    def getGameMetadata(gameServer:String):Future[Seq[GameMetadata]] = {
        for
            response <- dom.fetch(serverRoot + "/games/" + gameServer) 
            json <- response.json()
        yield 
            json.asInstanceOf[js.Array[GameMetadata]].toSeq            
    }

    /** A similar main menu sequence as the Swing client has */
    def runMainMenuSequence():Unit = {
        MainWindow.showStarfieldMessage("Minigame Network")

        for
            pong <- ping()
            servers <- getGameServers()
        do MainWindow.showGameServers(servers)
    }

}