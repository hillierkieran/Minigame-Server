package minigames.client;

import io.vertx.core.json.JsonObject;
import minigames.rendering.GameMetadata;
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
     * 
     * @param renderingPackage
     */
    public void load(MinigameNetworkClient mnClient, GameMetadata game, String player);

    /**
     * Called to execute a command that has been sent by the server
     * @param renderingPackage
     */
    public void execute(GameMetadata game, JsonObject command);

    /** 
     * Usually this is at the end of the game 
     */
    public void closeGame();
    
}
