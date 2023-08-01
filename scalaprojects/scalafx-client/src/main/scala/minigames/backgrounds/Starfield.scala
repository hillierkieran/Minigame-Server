package minigames.backgrounds

import scalafx.scene.canvas.Canvas
import scala.util.Random
import scalafx.scene.paint.Color
import scalafx.application.Platform

import minigames.scalafxclient.*

class Starfield() extends Tickable {

    private var stars = Seq.fill(100)(
        Star(Random.nextInt(800), Random.nextInt(600), Random.nextDouble(), Random.nextDouble())
    )

    case class Star(x:Int, y:Int, hue:Double, brightness:Double) {
        def colour = scalafx.scene.paint.Color.hsb(Random.nextInt(360), 0.5f, brightness)

        def mutate = this.copy(brightness = Math.max(0, Math.min(1, brightness - 0.1f + 0.2f * Random.nextDouble())))
    }

    val canvas = new Canvas {
        height = 600
        width = 800
    }

    def paint() = {
        val gc = canvas.getGraphicsContext2D()
        gc.setFill(Color.Black)
        gc.fillRect(0, 0, 800, 600);

        for s <- stars do
            gc.setFill(s.colour)
            gc.fillRect(s.x, s.y, 2, 2)

    }

    override def tick(now:Long, delta:Long) = 
        if (canvas.isVisible()) then
            stars = stars.map(_.mutate)
            Platform.runLater(() => paint())
            Animator.requestTick(this)

    Animator.requestTick(this)

}