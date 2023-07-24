package minigames.scalajsclient

import scala.collection.mutable

/**
 * Again, we have a client registry
 */
object ClientRegistry {

    private val clients:mutable.Map[String, GameClient] = mutable.Map.empty

    def registerClient(name:String, client:GameClient):Unit = {
        clients(name) = client
    }

    def getClient(name:String) = clients.get(name)

}