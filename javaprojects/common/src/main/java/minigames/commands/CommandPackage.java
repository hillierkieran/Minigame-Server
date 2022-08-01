package minigames.commands;

import java.util.List;
import io.vertx.core.json.JsonObject;

/**
 * A package of commands sent from a player to a game.
 * 
 * Commands are just given as a list of JSON objects. 
 * This lets GameClients and GameServers implement bespoke commands at will.
 */
public record CommandPackage(
    String gameServer,
    String gameId,
    String player,
    List<JsonObject> commands
) {
    
}
