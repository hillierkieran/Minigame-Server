package minigames.scalafxclient

import scalafx.animation.AnimationTimer

/**
 * Holds a list of items that need to be "tick"ed in the next tick cycle.
 * 
 * Note that the list is cleared at the start of the tick cycle. If you want another tick,
 * register yourself again. The task is fired by Vertx, so is not on the UI thread.
 */
object Animator {

    private var tickables:List[Tickable] = Nil
    private var last = 0L

    def requestTick(tickable:Tickable):Unit = 
        tickables = tickable :: tickables

    def requestTick(f:(Long, Long) => Unit) = 
        tickables = new Tickable { def tick(n:Long, d:Long) = f(n, d) } :: tickables

    def tick():Unit = {
        val now = System.nanoTime()
        val toTick = tickables
        val delta = now - last
        tickables = Nil
        for t <- toTick do t.tick(now, delta)
        last = now
    }

}