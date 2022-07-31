package minigames.client;

import java.util.ArrayList;

/**
 * Holds a list of items that need to be "tick"ed in the next tick cycle.
 * 
 * Note that the list is cleared at the start of the tick cycle. If you want another tick,
 * register yourself again. The task is fired by Vertx, so is not on the UI thread.
 */
public class Animator {

    long last = System.nanoTime();

    ArrayList<Tickable> tickables = new ArrayList<>();

    public void requestTick(Tickable tickable) {
        tickables.add(tickable);
    }

    public void tick() {
        long now = System.nanoTime();
        long delta = now - last;
        ArrayList<Tickable> toTick = tickables;
        tickables = new ArrayList<>();
        for (Tickable t : toTick) {
            t.tick(this, now, delta);
        }
        last = now;
    }
    
}
