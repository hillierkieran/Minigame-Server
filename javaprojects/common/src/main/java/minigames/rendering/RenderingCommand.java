package minigames.rendering;

import io.vertx.core.json.JsonObject;

/**
 * An interface representing a RenderingCommand.
 * 
 * You don't have to use this - you can just create JsonObjects directly. It is there for those that want
 * to use it, however
 */
public interface RenderingCommand {

    /** Converts this command to a JSON representation to send to the client */
    public JsonObject toJson();

}