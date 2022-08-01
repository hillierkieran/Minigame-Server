package minigames.client;

import minigames.rendering.RenderingPackage;


/**
 * A GameClient knows how to interpret rendering commands for one or more games.
 * 
 * There is not necessarily a one-to-one relationship between GameClients and GameServers.
 * For instance, we could have a generic client for text adventure games (but many different such
 * games on the server) and another generic client for sprite-based games (but many different such
 * games on the server).
 * 
 * Or you could define a dedicated GameClient for your game, so that it can understand bespoke 
 * rendering commands.
 * 
 * Most GameClients will implement Tickable, so they can be ticked by the Animator, but it is not
 * required. (e.g. a text adventure game might not.)
 */
public interface GameClient {

    /**
     * Called at start-up, to ensure that GameClients can access the hub for other services
     * @param mnClient
     */
    public void setMinigameNetworkClient(MinigameNetworkClient mnClient);

    /**
     * Called when a new game has been started.
     * @param name identifies this particular game in progress (e.g. "Joe's game against his sister")
     * @param game essentially identifies the game server (e.g. Turn-Based Pong)
     * @param players names of the players to display
     * @param renderingPackage rendering instructions
     */
    public void newGame(String name, String game, String players[], RenderingPackage renderingPackage);

    /** 
     * Called when a new game has been started 
     * @param name identifies this particular game in progress (e.g. "Joe's game against his sister")
     * @param game essentially identifies the game server (e.g. Turn-Based Pong)
     * @param players names of the players to display
     * @param renderingPackage rendering instructions
     */
    public void joinGame(String name, String game, String players[], RenderingPackage renderingPackage);

    /** 
     * Called when the game has a response to a request made from the client 
     * @param name identifies this particular game in progress (e.g. "Joe's game against his sister")
     * @param game essentially identifies the game server (e.g. Turn-Based Pong)
     * @param players names of the players to display
     * @param renderingPackage rendering instructions
     */
    public void serverReply(String name, String game, String players[], RenderingPackage renderingPackage);

    /** 
     * Usually this is at the end of the game 
     * @param name identifies this particular game in progress (e.g. "Joe's game against his sister")
     * @param game essentially identifies the game server (e.g. Turn-Based Pong)
     * @param players names of the players to display
     * @param renderingPackage rendering instructions (e.g. for a victory screen)
     */
    public void closeGame(String name, String game, String players[], RenderingPackage renderingPackage);
    
}
