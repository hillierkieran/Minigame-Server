package minigames.client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnimatorTest {
    
    /**
     * Test the animator is calling tick for Tickables that have requested it.
     * Also demonstrates the use of verify and any and eq matchers
     */
    @Test
    @DisplayName("Animator calls the tickables in its list")
    public void callsTickables() {
        Animator animator = new Animator();
        Tickable t1 = mock(Tickable.class);
        Tickable t2 = mock(Tickable.class);
        animator.requestTick(t1);
        animator.requestTick(t2);

        animator.tick();
        // In the test, we can't rely on the specific time interval (as it comes from System.now() and could vary)
        // so we give Mockito an "any" matcher for the times to say we're happy with any time it calls it with.
        // As we're using an explicit matcher for the "any"s, we also need to use an explicit "eq" matcher for the animator
        verify(t1).tick(eq(animator), anyLong(), anyLong());
        verify(t2).tick(eq(animator), anyLong(), anyLong());
    }

    /**
     * Test the animator is calling tick for Tickables that have requested it.
     * Also demonstrates the use of verify and any and eq matchers
     */
    @Test
    @DisplayName("Tickables must re-register with the Animator to get a secondTick")
    public void onlyCallsTickablesForCurrentTick() {
        Animator animator = new Animator();
        Tickable t1 = mock(Tickable.class);
        Tickable t2 = mock(Tickable.class);
        animator.requestTick(t1);
        animator.requestTick(t2);

        animator.tick();
        animator.requestTick(t2);
        animator.tick();

        // t1 should be ticked once (i.e. on the first tick only)
        verify(t1, times(1)).tick(eq(animator), anyLong(), anyLong());
        // t2 should be ticked twice (i.e. on both ticks)
        verify(t2, times(2)).tick(eq(animator), anyLong(), anyLong());
    }

}
