package minigames.rendering;

/**
 * Basic data about a game in progress.
 */
public record GameMetadata(
    String gameServer,
    String name,
    String[] players,
    boolean joinable
) {
    
}
