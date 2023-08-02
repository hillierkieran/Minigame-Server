package minigames.rendering

import io.vertx.core.json.JsonObject

/**
  * Basic data about a game in progress
  */
case class GameMetadata(gameServer:String, name:String, players:Array[String], joinable:Boolean)

