package minigames.server.muddle;

import java.util.*;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import minigames.rendering.*;

/**
 * Represents an actual Muddle game in progress
 */
public class MuddleGame {

    record MuddlePlayer(
        int x,
        int y
    ) {    
    }

    /** Uniquely identifies this game */
    String name;

    public MuddleGame(String name) {
        this.name = name;
    }


    String[][] rooms = new String[][] {
        {
            "A maze of twisting passages, all alike",
            "Ok, they weren't so alike after all"
        }
    };

    HashMap<String, MuddlePlayer> players = new HashMap<>();

    /** The players currently playing this game */
    public String[] getPlayerNames() {
        return players.keySet().toArray(String[]::new);
    }

    /** Metadata for this game */
    public GameMetadata gameMetadata() {
        return new GameMetadata("Muddle", name, getPlayerNames(), true);
    }

    public RenderingPackage joinGame(String playerName) {
        if (players.containsKey(playerName)) {
            return new RenderingPackage(
                gameMetadata(),
                Arrays.stream(new RenderingCommand[] {
                    new NativeCommands.ShowMenuError("That name's not available")
                }).map((r) -> r.toJson()).toList()
            );
        } else {
            players.put(playerName, new MuddlePlayer(0, 0));

            ArrayList<RenderingCommand> commands = new ArrayList<>();
            commands.add(new NativeCommands.LoadClient("MuddleText", "Muddle", name, playerName));

            // FIXME: Add commands that render the content!

            return new RenderingPackage(gameMetadata(), commands.stream().map((r) -> r.toJson()).toList());
        }

    }
    
}
