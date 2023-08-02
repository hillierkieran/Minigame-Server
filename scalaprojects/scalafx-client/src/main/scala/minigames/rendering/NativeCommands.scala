package minigames.rendering

import io.vertx.core.json.JsonObject
import scala.util.*

extension (json:JsonObject) {
    def readString(name:String):Try[String] = 
        Option(json.getString(name)) match {
            case Some(v) => Success(v)
            case None => Failure(NoSuchFieldException(s"Json had no field $name"))
        }

    def readObject(name:String):Try[JsonObject] = 
        Option(json.getJsonObject(name)) match {
            case Some(v) => Success(v)
            case None => Failure(NoSuchFieldException(s"Json had no field $name"))
        }
}

enum NativeCommand:
    case LoadClient(clientName:String, gameServer:String, game:String, player:String)
    case ShowMenuError(message:String)
    case QuitToMenu

object NativeCommand:
    /** Tries to parse this as a native command. Note the type signature is a little different than the Java version */
    def tryParsing(json:JsonObject):Option[Try[NativeCommand]] = 
        Option(json.getString("nativeCommand")) match {
            case Some("client.loadClient") => Some(
                for 
                    clientName <- json.readString("clientName")
                    gameServer <- json.readString("gameServer")
                    game <- json.readString("game")
                    player <- json.readString("player")
                yield LoadClient(clientName, gameServer, game, player)
            )
            case Some("client.showMenuError") => Some(
                for 
                    message <- json.readString("message")
                yield ShowMenuError(message)
            )
            case Some("client.quitToMenu") => Some(Success(QuitToMenu))
            case _ => None
        }
