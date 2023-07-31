package minigames.scalafxclient

import minigames.rendering.*

import io.vertx.core.json.JsonObject

/**
 * A GameClient knows how to interpret rendering commands for one or more games.
 * 
 * There is not necessarily a one-to-one relationship between GameClients and GameServers.
 * For instance, we could have a generic client for text adventure games (but many different such
 * games on the server) and another generic client for sprite-based games (but many different such
 * games on the server).
 * 
 * Or you could define a dedicated GameClient for your game, so that it can understand bespoke 
 * rendering commands.
 * 
 * Most GameClients will implement Tickable, so they can be ticked by the Animator, but it is not
 * required. (e.g. a text adventure game might not.)
 */
trait GameClient {

    def load(client:MinigameNetworkClient, game:GameMetadata, player:String):Unit

    def closeGame():Unit

    def execute(metadata:GameMetadata, command:JsonObject):Unit

}