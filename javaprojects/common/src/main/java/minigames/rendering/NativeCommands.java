package minigames.rendering;

import java.util.Optional;
import io.vertx.core.json.JsonObject;

/**
 * The only commands known natively to MinigameNetworkClient
 */
public class NativeCommands {

    /** Loads a client */
    public record LoadClient(String clientName, String gameServer, String game, String player) implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject()
                .put("nativeCommand", "client.loadClient")
                .put("clientName", clientName)
                .put("gameServer", gameServer)
                .put("game", game)
                .put("player", player);
        }

        /** Attempts to parse a json object, returning a filled Optional if it found a LoadClient command, and an empty one otherwise */
        public static Optional<LoadClient> tryParsing(JsonObject json) {
            if (json.getString("nativeCommand") == null) return Optional.empty();
            
            return switch (json.getString("nativeCommand")) {
                case "client.loadClient" -> Optional.of(new LoadClient(
                    json.getString("clientName"), 
                    json.getString("gameServer"), 
                    json.getString("game"), 
                    json.getString("player"))
                );
                default -> Optional.empty();
            };
        }
    }

    /** Used for showing errors before joining a game, e.g. if a player name is not available */
    public record ShowMenuError(String message) implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject()
                .put("nativeCommand", "client.showMenuError")
                .put("message", message);
        }

        /** Attempts to parse a json object, returning a filled Optional if it found a ShowMenuError command, and an empty one otherwise */
        public static Optional<ShowMenuError> tryParsing(JsonObject json) {
            if (json.getString("nativeCommand") == null) return Optional.empty();

            return switch (json.getString("nativeCommand")) {
                case "client.showMenuError" -> Optional.of(new ShowMenuError(
                    json.getString("message")
                ));
                default -> Optional.empty();
            };
        }
    }

    /** Quits to the main menu */
    public record QuitToMenu() implements RenderingCommand {
        public JsonObject toJson() {
            return new JsonObject().put("nativeCommand", "client.quitToMGNMenu");
        }

        /** Attempts to parse a json object, returning a filled Optional if it found a QuitToMenu command, and an empty one otherwise */
        public static Optional<QuitToMenu> tryParsing(JsonObject json) {
            if (json.getString("nativeCommand") == null) return Optional.empty();

            return switch (json.getString("nativeCommand")) {
                case "client.quitToMGNMenu" -> Optional.of(new QuitToMenu());
                default -> Optional.empty();
            };
        }
    }
    
}
