package minigames.server.muddle;

import io.vertx.core.Future;
import minigames.commands.CommandPackage;
import minigames.rendering.GameServerDetails;
import minigames.rendering.RenderingPackage;
import minigames.server.ClientType;
import minigames.server.GameMetadata;
import minigames.server.GameServer;

public class MuddleServer implements GameServer {

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
        // TODO Auto-generated method stub
        return null;
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
