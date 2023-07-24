package minigames.scalajsclient

import org.scalajs.dom
import com.wbillingsley.veautiful.html.{Attacher, <}
import minigames.scalajsclient.backgrounds.Starfield
import minigames.scalajsclient.muddletext.MuddleText


/** A place for wiring things up */
def doWiring():Unit = {

  ClientRegistry.registerClient("MuddleText", MuddleText)

}

@main def main():Unit = {
    // Install our CSS styles in the page
    styleSuite.install();

    // Render the main "window"
    val attacher = Attacher.newRoot(dom.document.getElementById("render-here"))
    attacher.render(MainWindow)

    doWiring()

    MinigameNetworkClient.runMainMenuSequence()
}