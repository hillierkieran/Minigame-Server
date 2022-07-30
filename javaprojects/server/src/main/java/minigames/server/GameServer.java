package minigames.server;

import io.vertx.core.Future;
import minigames.rendering.RenderingPackage;
import minigames.commands.CommandPackage;

/**
 * Common interface for minigame servers to implement.
 */
public interface GameServer {

    /** Return basic metadata to show to the client */
    public GameMetadata getMetadata();
    
    /** What kinds of client can this game be played in? */
    public ClientType[] getSupportedClients();

    /** Data on what games are in progress, for players wishing to join */
    public GameMetadata[] getGamesInProgress();

    /** 
     * Creates a new game and returns the rendering package for a client.
     * Returns a Future because in Vertx we don't want to block the event loop.
     * See the example for how to return a Future
     */
    public Future<RenderingPackage> newGame();

    /** 
     * Joins a game in progress and returns the rendering package for a client.
     * Returns a Future because in Vertx we don't want to block the event loop.
     * See the example for how to return a Future
     */
    public Future<RenderingPackage> joinGame(String game, String player);

    /** 
     * Effectively called by the client when it wants to make a move. 
     */
    public Future<RenderingPackage> callGame(String game, String player, CommandPackage commands);

}
