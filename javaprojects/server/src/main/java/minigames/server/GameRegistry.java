package minigames.server;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * Holds information on which games are available and can be served up to clients.
 */
public class GameRegistry {

    private HashMap<String, GameServer> gameServers = new HashMap<>();

    /**
     * Called by your GameServer to register it as being available to play
     * @param name 
     * @param gs
     */
    public void registerGameServer(String name, GameServer gs) {
        gameServers.put(name, gs);
    }

    /** Gets a GameServer from the registry */
    public GameServer getGameServer(String name) {
        return gameServers.get(name);
    }

    /**
     * Called by the MinigameNetworkServer when a client is asking what games are available for it.
     * @param platform
     * @return
     */
    public List<GameServer> getGamesForPlatform(ClientType platform) {
        return gameServers.values().stream()
            .filter((gs) -> Arrays.asList(gs.getSupportedClients()).contains(platform))
            .toList();
    }
    
}
