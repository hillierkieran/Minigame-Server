package minigames.client;


import java.util.HashMap;

/**
 * Holds information on which games are available and can be served up to clients.
 */
public class ClientRegistry {

    private HashMap<String, GameClient> gameClients = new HashMap<>();

    /**
     * Called by your GameServer to register it as being available to play
     * @param name 
     * @param gs
     */
    public void registerGameClient(String name, GameClient gc) {
        gameClients.put(name, gc);
    }

    public GameClient getGameClient(String name) {
        return gameClients.get(name);
    }

}
