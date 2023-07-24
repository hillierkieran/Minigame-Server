package minigames.scalajsclient.backgrounds

import org.scalajs.dom
import scala.util.Random

/**
 * A starfield background rendered using the raw dom API.
 */
class Starfield {

    /** 
     * The canvas element we will render 
     * Unlike Swing, we can't "extend Canvas". We have to create a canvas element and control it from the outside.
     * The code for creating a canvas is kept as similar as possible to the Scala-js-dom documentation here:
     * https://scala-js.github.io/scala-js-dom/
     */
    val canvas = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]

    canvas.width = 800
    canvas.height = 600

    /** A star. A case class is very similar to a Java record. */
    case class Star(x:Int, y:Int, hue:Float, brightness:Float) {
        // Produce a css colour
        def hsl = s"hsl(${(hue * 360).toInt} 100% ${(brightness * 100).toInt}%)"

        // Randomly alter the brightness
        def mutate = this.copy(brightness = Math.max(0, Math.min(1, brightness - 0.2f + Random.nextFloat() * 0.4f)))
    }

    // Create 100 stars
    // Veterans of my Scala unit will be familiar with this sort of one-liner!
    var stars = (1 to 100).map(_ => Star(Random.nextInt(800), Random.nextInt(600), Random.nextFloat(), Random.nextFloat()))

    // Our paint method
    def paint() = {
        // This code is kept as similar as possible to the scala-js-dom docs at
        // https://scala-js.github.io/scala-js-dom/
        type Ctx2D = dom.CanvasRenderingContext2D
        val ctx = canvas.getContext("2d").asInstanceOf[Ctx2D]

        // Clear the sky
        ctx.fillStyle = "black"
        ctx.fillRect(0, 0, 800, 600)

        // Paint the stars
        for s <- stars do
            ctx.fillStyle = s.hsl
            ctx.fillRect(s.x, s.y, 2, 2)
    }

    // Starts the stars a-twinking by calling the first tick
    def start():Unit = tick()

    // Called by the animator - in this case, the browser's own requestAnimationFrame method
    def tick():Unit = {
        // Twinkle all the stars
        stars = stars.map(_.mutate)

        // Repaint
        paint()

        // Only ask for a tick if we're in the document
        if (canvas.parentElement != null) {
            // Browsers have "requestAnimationFrame" built in, so there's no separate Animator class
            dom.window.requestAnimationFrame((_) => tick())
        }
    }

}