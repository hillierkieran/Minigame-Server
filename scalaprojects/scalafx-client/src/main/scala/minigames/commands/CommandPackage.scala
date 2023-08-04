package minigames.commands

import io.vertx.core.json.JsonObject
import scala.jdk.CollectionConverters.*
import io.vertx.core.json.JsonArray

case class CommandPackage(
    gameServer:String,
    gameId:String,
    player:String,
    commands:Seq[JsonObject]
) {

    def toJson = JsonObject()
        .put("gameServer", gameServer)
        .put("gameId", gameId)
        .put("player", player)
        .put("commands", {
            commands.foldLeft(JsonArray()) { _.add(_) }
        })

}

object CommandPackage {
    def fromJson(json:JsonObject) = CommandPackage(
        gameServer=json.getString("gameServer"),
        gameId=json.getString("gameId"),
        player=json.getString("player"),
        commands=json.getJsonArray("commands").toSeq[JsonObject]
    )
}