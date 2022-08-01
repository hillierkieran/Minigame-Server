package minigames.rendering;

import java.util.List;
import io.vertx.core.json.JsonObject;

/**
 * Returned by a game to update the UI state of the client.
 * 
 * Rendering commands are just given as a list of JSON objects. 
 * This lets GameClients and GameServers implement bespoke rendering commands at will.
 * 
 * There are a small set of rendering commands known natively to the client
 */
public record RenderingPackage(
    GameMetadata metadata,
    List<JsonObject> renderingCommands
) {

  public static RenderingPackage fromJson(JsonObject json) {
    return new RenderingPackage(
        json.getJsonObject("metadata").mapTo(GameMetadata.class),
        json.getJsonArray("renderingCommands").stream().map((o) -> (JsonObject)o).toList()
    );
  }

}
