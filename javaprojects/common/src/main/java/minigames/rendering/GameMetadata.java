package minigames.rendering;

/**
 * Basic data about a game in progress.
 */
public record GameMetadata(
    String name,
    String game,
    String[] players,
    boolean joinable
) {
    
}
