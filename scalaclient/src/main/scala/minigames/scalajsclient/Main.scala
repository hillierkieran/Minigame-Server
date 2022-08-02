package minigames.scalajsclient

import org.scalajs.dom
import com.wbillingsley.veautiful.html.{Attacher, <}
import minigames.scalajsclient.backgrounds.Starfield

@main def main():Unit = {
    // Install our CSS styles in the page
    styleSuite.install();

    // Render the main "window"
    val attacher = Attacher.newRoot(dom.document.getElementById("render-here"))
    attacher.render(MainWindow)

    MinigameNetworkClient.runMainMenuSequence()
}