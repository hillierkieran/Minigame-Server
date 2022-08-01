package minigames.rendering;

import io.vertx.core.json.JsonObject;

public interface RenderingCommand {
    public JsonObject toJson();
}
