package minigames.server.muddle;

import io.vertx.core.Future;
import minigames.commands.CommandPackage;
import minigames.rendering.GameMetadata;
import minigames.rendering.GameServerDetails;
import minigames.rendering.RenderingPackage;
import minigames.server.ClientType;
import minigames.server.GameServer;

import java.util.HashMap;

/**
 * An essentially empty placeholder game used in the writing of the starter code.
 */
public class MuddleServer implements GameServer {

    /** Holds the games in progress in memory (no db) */
    HashMap<String, MuddleGame> games = new HashMap<>();

    @Override
    public GameServerDetails getDetails() {
        return new GameServerDetails("Muddle", "It would be a MUD, but it's not really written yet");
    }

    @Override
    public ClientType[] getSupportedClients() {
        return new ClientType[] { ClientType.Swing, ClientType.Scalajs };
    }

    @Override
    public GameMetadata[] getGamesInProgress() {
        return games.keySet().stream().map((name) -> {
            return new GameMetadata(name, "Muddle", games.get(name).getPlayerNames(), true);
        }).toArray(GameMetadata[]::new);
    }

    @Override
    public Future<RenderingPackage> newGame() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RenderingPackage> joinGame(String game, String player) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RenderingPackage> callGame(String game, String player, CommandPackage commands) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
