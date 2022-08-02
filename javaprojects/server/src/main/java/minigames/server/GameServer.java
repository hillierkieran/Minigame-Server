package minigames.server;

import io.vertx.core.Future;
import minigames.rendering.GameMetadata;
import minigames.rendering.GameServerDetails;
import minigames.rendering.RenderingPackage;
import minigames.commands.CommandPackage;

/**
 * Common interface for minigame servers to implement.
 */
public interface GameServer {
    
    /** Unique and human-readable (we don't have that many) */
    public GameServerDetails getDetails();

    /** What kinds of client can this game be played in? */
    public ClientType[] getSupportedClients();

    /** Data on what games are in progress, for players wishing to join */
    public GameMetadata[] getGamesInProgress();

    /** 
     * Creates a new game and returns the rendering package for a client.
     * Returns a Future because in Vertx we don't want to block the event loop.
     * See the example for how to return a Future
     * 
     * The player name is passed as a parameter, but the GameServer will typically allocate
     * the game a generated name.
     */
    public Future<RenderingPackage> newGame(String playerName);

    /** 
     * Joins a game in progress and returns the rendering package for a client.
     * Returns a Future because in Vertx we don't want to block the event loop.
     * See the example for how to return a Future
     */
    public Future<RenderingPackage> joinGame(String game, String player);

    /** 
     * Effectively called by the client when it wants to make a move. 
     */
    public Future<RenderingPackage> callGame(CommandPackage commands);

}
