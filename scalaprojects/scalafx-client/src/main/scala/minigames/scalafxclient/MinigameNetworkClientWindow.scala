package minigames.scalafxclient

import scalafx.scene.layout.*
import javafx.scene.control.TextField
import javafx.scene.Node
import scalafx.scene.Scene

/**
 * The main window that appears.
 * 
 * For simplicity, we give it a BorderLayout with panels for north, south, east, west, and center.
 * 
 * This makes it simpler for games to load up the UI however they wish, though the default expectation
 * is that the centre just has an 800x600 canvas.
 */
class MinigameNetworkClientWindow(val mgnClient: MinigameNetworkClient) {

    val north = HBox()
    val south = HBox()
    val east = HBox()
    val west = HBox()
    val center = HBox()

    val all = Seq(center, north, east, south, west)

    val borderPane = BorderPane(center, north, east, south, west)

    // We hang on to this one for registering in servers
    val nameField = TextField("Algernon")

    def clearNorth():Unit = north.children.clear()
    def clearSouth():Unit = south.children.clear()
    def clearEast():Unit = east.children.clear()
    def clearWest():Unit = west.children.clear()
    def clearCenter():Unit = center.children.clear()

    def addNorth(node:Node) = north.children.append(node)
    def addSouth(node:Node) = south.children.append(node)
    def addEast(node:Node) = east.children.append(node)
    def addWest(node:Node) = west.children.append(node)
    def addCenter(node:Node) = center.children.append(node)

    def clearAll():Unit = for p <- all do p.children.clear()

    /** Makes the main window visible */
    def show():Unit = 
        App.stage.scene = new Scene {
            content = borderPane
        }
        App.stage.show()


}