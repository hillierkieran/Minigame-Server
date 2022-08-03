package minigames.scalajsclient

import scalajs.js

/** 
 * Trait implemented by GameClients in Scalaland.
 * 
 * As things like the ClientRegistry are objects (globally available), we have slightly
 * fewer arguments
 */
trait GameClient {

    def load(metadata:GameMetadata, playerName:String):Unit

    def closeGame():Unit

    def execute(metadata:GameMetadata, command:js.Dynamic):Unit

}