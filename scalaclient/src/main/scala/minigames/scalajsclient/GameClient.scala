package minigames.scalajsclient

/** Basic data about a game in progress */
case class GameMetadata(gameServer:String, name:String, players:Seq[String], joinable:Boolean)

/** Basic data about a game server */
case class GameServerDetails(name:String, description:String)

/** 
 * Trait implemented by GameClients in Scalaland.
 * 
 * As things like the ClientRegistry are objects (globally available), we have slightly
 * fewer arguments
 */
trait GameClient {

    def load(metadata:GameMetadata, playerName:String):Unit

}