package minigames.rendering;

import io.vertx.core.json.JsonObject;

/**
 * The only commands known natively to MinigameNetworkClient
 */
public class NativeCommands {

    public record LoadClient(String clientName, String gameServer, String game, String player) implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject()
                .put("command", "loadClient")
                .put("clientName", clientName)
                .put("gameServer", gameServer)
                .put("game", game)
                .put("player", player);
        }
    }

    /** Used for showing errors before joining a game, e.g. if a player name is not available */
    public record ShowMenuError(String message) implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject()
                .put("command", "showMenuError")
                .put("message", message);
        }
    }

    public record QuitToMenu() implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject().put("command", "quitToMGNMenu");
        }
    }
    
}
