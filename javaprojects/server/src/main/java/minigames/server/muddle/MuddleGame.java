package minigames.server.muddle;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import minigames.commands.CommandPackage;
import minigames.rendering.*;
import minigames.rendering.NativeCommands.LoadClient;

/**
 * Represents an actual Muddle game in progress
 */
public class MuddleGame {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(MuddleGame.class);

    static int WIDTH = 2;
    static int HEIGHT = 2;

    record MuddlePlayer(
        String name,
        int x, int y,
        List<String> inventory
    ) {    
    }

    /** Uniquely identifies this game */
    String name;

    public MuddleGame(String name) {
        this.name = name;
    }

    String[][] rooms = new String[][] {
        {
            "You are in a maze of twisting passages, all alike",
            "You are in a maze of twisting passages that weren't so alike after all"
        },
        {
            "You are standing in an open field west of a white house, with a boarded front door. There is a small mailbox here.",
            "You wake up. The room is very gently spinning around your head. Or at least it would be if you could see it which you can't. It is pitch black."
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

    /** Describes the state of a player */
    private String describeState(MuddlePlayer p) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("[%d,%d] \n\n", p.x, p.y));
        sb.append(rooms[p.x()][p.y()]);

        return sb.toString();
    }

    private String directions(int x, int y) {
        String d = "";
        if (x > 0) d += "W";
        if (y > 0) d += "N";
        if (x < WIDTH - 1) d += "E";
        if (x < HEIGHT - 1) d += "S";

        return d;
    }


    public RenderingPackage runCommands(CommandPackage cp) {   
        logger.info("Received command package {}", cp);     
        MuddlePlayer p = players.get(cp.player());

        // FIXME: Need to actually run the commands!

        ArrayList<JsonObject> renderingCommands = new ArrayList<>();
        renderingCommands.add(new JsonObject().put("command", "clearText"));
        renderingCommands.add(new JsonObject().put("command", "appendText").put("text", describeState(p)));
        renderingCommands.add(new JsonObject().put("command", "setDirections").put("directions", directions(p.x(), p.y())));

        return new RenderingPackage(this.gameMetadata(), renderingCommands);
    }

    /** Joins this game */
    public RenderingPackage joinGame(String playerName) {
        if (players.containsKey(playerName)) {
            return new RenderingPackage(
                gameMetadata(),
                Arrays.stream(new RenderingCommand[] {
                    new NativeCommands.ShowMenuError("That name's not available")
                }).map((r) -> r.toJson()).toList()
            );
        } else {
            MuddlePlayer p = new MuddlePlayer(playerName, 0, 0, List.of());
            players.put(playerName, p);

            ArrayList<JsonObject> renderingCommands = new ArrayList<>();
            renderingCommands.add(new LoadClient("MuddleText", "Muddle", name, playerName).toJson());
            renderingCommands.add(new JsonObject().put("command", "clearText"));
            renderingCommands.add(new JsonObject().put("command", "appendText").put("text", describeState(p)));
            renderingCommands.add(new JsonObject().put("command", "setDirections").put("directions", directions(p.x(), p.y())));

            return new RenderingPackage(gameMetadata(), renderingCommands);
        }

    }
    
}
