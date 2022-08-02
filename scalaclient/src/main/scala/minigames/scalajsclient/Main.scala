package minigames.scalajsclient

import org.scalajs.dom
import com.wbillingsley.veautiful.html.{Attacher, <}
import minigames.scalajsclient.backgrounds.Starfield

@main def main():Unit = {

    // Get the MGN server location from the search string
    val serverUrl = dom.window.location.search.drop(1)
    dom.console.log("MGN server url", serverUrl)

    // Install our CSS styles in the page
    styleSuite.install();

    // Render the main "window"
    val attacher = Attacher.newRoot(dom.document.getElementById("render-here"))
    attacher.render(MainWindow)

    val s = new Starfield()
    MainWindow.center.add(s.canvas)

    val l = dom.document.createElement("label")
    l.innerText = "Minigame Network"
    l.setAttribute("style", "position: absolute; top: 318px; font-family: monospace; font-size: 36px; color: 'cyan';")
    MainWindow.center.add(l)

    s.start()

}