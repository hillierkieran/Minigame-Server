package minigames.commands

import io.vertx.core.json.JsonObject
import scala.jdk.CollectionConverters.*

case class CommandPackage(
    gameServer:String,
    gameId:String,
    player:String,
    commands:Seq[JsonObject]
) {

}

object CommandPackage {
    def fromJson(json:JsonObject) = CommandPackage(
        gameServer=json.getString("gameServer"),
        gameId=json.getString("gameId"),
        player=json.getString("player"),
        commands=json.getJsonArray("commands").toSeq[JsonObject]
    )
}