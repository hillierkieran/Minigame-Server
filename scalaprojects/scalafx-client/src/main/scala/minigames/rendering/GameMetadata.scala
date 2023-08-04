package minigames.rendering

import io.vertx.core.json.JsonObject
import scala.util.Try
import minigames.commands.mapper

import scala.jdk.CollectionConverters.*
import io.vertx.core.json.JsonArray


/**
  * Basic data about a game in progress
  */
case class GameMetadata(gameServer:String, name:String, players:Array[String], joinable:Boolean)

object GameMetadata {

  def fromJson(json:JsonObject):GameMetadata = 
    GameMetadata(
      gameServer = json.getString("gameServer"),
      name = json.getString("name"),
      players = (for 
        case s: String <- json.getJsonArray("players").iterator.asScala
      yield s).toArray,
      joinable = json.getBoolean("joinable")
    )

  def fromJsonArray(json:JsonArray):Seq[GameMetadata] = 
    (for
      case o:JsonObject <- json.iterator().asScala 
    yield GameMetadata.fromJson(o)).toSeq
}
