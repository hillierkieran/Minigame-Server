package minigames.client;

/**
 * Indicates that this class can receive a UI update "tick" from the timer
 */
public interface Tickable {

    public void tick(Animator al, long now, long delta);
    
}
