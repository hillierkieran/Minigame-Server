package minigames.commands

import io.vertx.core.json.*
import scala.jdk.CollectionConverters.*

case class GameServerDetails(name:String, description:String)

object GameServerDetails {

    def fromJson(json:JsonObject) = GameServerDetails(
        json.getString("name"),
        json.getString("description")
    )

    def fromJsonArray(json:JsonArray):Seq[GameServerDetails] = 
        (for
            case o:JsonObject <- json.iterator().asScala 
        yield fromJson(o)).toSeq

}