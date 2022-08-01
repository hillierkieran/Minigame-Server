package minigames.rendering;

import io.vertx.core.json.JsonObject;

/**
 * Largely a convenience 
 */
public interface RenderingCommand {
    public JsonObject toJson();

}