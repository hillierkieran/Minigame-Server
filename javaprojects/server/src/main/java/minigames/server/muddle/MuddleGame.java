package minigames.server.muddle;

import java.util.HashMap;

/**
 * Represents an actual Muddle game in progress
 */
public class MuddleGame {

    String name;

    record MuddlePlayer(
        int x,
        int y
    ) {
    
    }

    String[][] rooms = new String[][] {
        {
            "A maze of twisting passages, all alike",
            "Ok, they weren't so alike after all"
        }
    };

    HashMap<String, MuddlePlayer> players = new HashMap<>();

    public String[] getPlayerNames() {
        return players.keySet().toArray(String[]::new);
    }
    
}
