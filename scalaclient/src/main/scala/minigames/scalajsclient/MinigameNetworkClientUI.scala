package minigames.scalajsclient

import org.scalajs.dom 
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global


/*
   My code for the main UI uses a rendering library I wrote. But you don't have to.
   You can just create HTML elements programmatically and add them. This is just a fast way
   for me to program the main client.
 */
import com.wbillingsley.veautiful.html.{VHtmlNode, VHtmlComponent, StyleSuite, Styling, <, ^}


/**
 * The main "window" 
 * 
 * As with the Swing client, it gives you a north, south, east, west, and center panel
 * 
 * You can add elements to them with, e.g.
 * 
 *   val b = dom.document.createElement("button")
 *   b.innerText = "Hello"
 *   MainWindow.north.add(el)
 * 
 */
object MainWindow extends VHtmlComponent() {

    val north = FlowPanel()
    val south = FlowPanel()
    val east = FlowPanel()
    val west = FlowPanel()
    val center = FlowPanel()

    val styling = Styling("""
    |display: inline-block;
    |""".stripMargin).modifiedBy(
        // Mimics JPanel's FlowLayout
        " .flowLayout" -> "display: flex; align-items: center; justify-content: center;",
        " .outer" -> "display: flex; flex-direction: column; border: 10px solid #f0f0f0; border-radius: 15px; margin: 30px; background: white;",
        " .north" -> "",
        " .middle" -> "",
        " .center" -> "display: inline-flex; width: 800px; height: 600px; align-items: center; justify-content: center;",
        " .south" -> "",

    ).register()

    def render = <.div(^.cls := styling.className,
      <.div(^.cls := "outer",
        <.div(^.cls := "north flowLayout", north),
        <.div(^.cls := "middle flowLayout",
            <.div(^.cls := "west flowLayout", west),
            <.div(^.cls := "center", center),
            <.div(^.cls := "east flowLayout", east)
        ),
        <.div(^.cls := "south flowLayout", south),
      )
    )

    /** Clears all the panel slots */
    def clear():Unit = {
        for panel <- Seq(north, south, east, west, center) do panel.clear()
    }

    /** Shows a short message over a retro style star field */
    def showStarfieldMessage(message:String):Unit = {
        clear()

        val s = backgrounds.Starfield()
        center.add(s.canvas)

        val l = dom.document.createElement("label")
        l.innerText = message
        l.setAttribute("style", "position: absolute; top: 318px; font-family: monospace; font-size: 36px; color: cyan;")
        center.add(l)

        s.start()
    }

    /** shows a list of available game servers */
    def showGameServers(servers:Seq[GameServerDetails]):Unit = {
        clear()

        // Sorry, this is lazy of me but I'm using my little kit for this one
        center.add(<.div(
            for gsd <- servers yield <.div(
                <.h4(gsd.name),
                <.p(gsd.description),
                <.div(
                    <.button("Open games", ^.onClick --> {
                        for games <- MinigameNetworkClient.getGameMetadata(gsd.name) do
                            showGames(gsd.name, games)
                    })
                )
            )
        ))
    }

        /** shows a list of available game servers */
    def showGames(gameServer:String, games:Seq[GameMetadata]):Unit = {
        clear()

        // Sorry, this is lazy of me but I'm using my little kit for this one
        center.add(<.div(
            for game <- games yield <.div(
                <.h4(game.name),
                <.p(game.players.mkString(", "))            
            )
        ))
    }

}


/** 
 * This is going to let us install CSS diretly in the page from Scala.
 */
given styleSuite:StyleSuite = StyleSuite()


/** 
 * An adapter that lets us add raw dom elements to a Veautiful element
 * 
 * Used internally by FlowPanel, but left exposed so you can use it if you
 * really want.
 */
case class DirectNode(el: dom.Element) extends VHtmlNode {
    var domNode:Option[dom.Element] = None

    def attach(): dom.Element = {
        domNode = Some(el)
        el
    }

    def detach(): Unit = { domNode = None }
}

/**  
 * A component that will attempt to mimic a JPanel with a FlowLayout.
 * 
 * It has two methods: add and clear
 * add can take a dom Element, or it can also take a VNode (something from my library)
 */
class FlowPanel(cssClass:String = "flowLayout") extends VHtmlNode {

    val children = mutable.Buffer.empty[<.ElementChild[dom.html.Div]]

    // What an empty one looks like
    def base = <.div(^.cls := cssClass)

    // The DiffNode we're controlling
    val controlNode = base

    export controlNode.attach
    export controlNode.detach
    export controlNode.domNode

    /** Adds a VNode to this panel */
    def add(vnode: VHtmlNode):Unit = {
        children.append(vnode)
        controlNode.makeItSo(base(children.toSeq*))
    }

    /** Adds a dom element to this panel */
    def add(el: dom.Element):Unit = {
        children.append(DirectNode(el))
        controlNode.makeItSo(base(children.toSeq*))
    }

    /** Clears all the child elements from this panel */
    def clear():Unit = {
        children.clear();
        controlNode.makeItSo(base(children.toSeq*))
    }

}







/** Not currently used. */
class RawNodeParent() extends VHtmlNode {

    val controlNode = <.div(^.cls := "flowLayout")

    export controlNode.attach
    export controlNode.detach
    export controlNode.domNode

    /** removes all child nodes from this element */
    def clear():Unit = {
        for el <- controlNode.domNode do 
            while el.firstChild != null do
                el.removeChild(el.firstChild)
    }

    def add(el:dom.html.Element):Unit = {
        for cn <- controlNode.domNode do
            cn.appendChild(el)
    }

}