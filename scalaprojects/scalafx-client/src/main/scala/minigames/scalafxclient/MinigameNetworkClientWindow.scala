package minigames.scalafxclient

import scalafx.scene.layout.*
import scalafx.scene.control.TextField
import scalafx.scene.Node
import scalafx.scene.Scene
import minigames.backgrounds.Starfield
import scalafx.scene.text.Text
import scalafx.geometry.VPos
import scalafx.scene.text.TextAlignment
import scalafx.scene.text.Font
import scalafx.scene.paint.Color

import minigames.rendering.*
import minigames.commands.*
import scalafx.scene.control.Label
import scalafx.scene.control.Button
import scalafx.application.Platform
import scalafx.geometry.Pos

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

    def all = Seq(center, north, east, south, west)

    val borderPane = BorderPane(center, north, east, south, west)

    // We hang on to this one for registering in servers
    val nameField = new TextField { text = "Algernon" }

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

    /** Shows a twinkling background with a label over it */
    def showStarfieldMessage(message:String):Unit = Platform.runLater {
        clearAll()
        addCenter(new StackPane {
            children = Seq(
                Starfield().canvas,
                new Text {
                    x = 400
                    y = 300
                    textOrigin = VPos.CENTER
                    textAlignment = TextAlignment.CENTER
                    text = message
                    font = Font.apply("Monospaced", 36)
                    fill = Color.Cyan
                }
            )
        })
        App.stage.sizeToScene()
        
    }

    /** Shows the list of game servers available for this client */
    def showGameServers(servers:Seq[GameServerDetails]):Unit = Platform.runLater {
        clearAll()
        addCenter(new VBox {
            alignment = Pos.CENTER
            children = 
                for s <- servers yield
                    new VBox {
                        children = Seq(
                            new Label {
                                text = s.name
                            },
                            new Label {
                                text = s.description
                            },
                            new Button {
                                text = "Open games"
                                onAction = (_) => {
                                    mgnClient.getGameMetadata(s.name).onSuccess((l) => showGames(s.name, l))
                                }
                            }
                        )
                    }
        })

    }

    def showGames(gameServer:String, games:Seq[GameMetadata]) = Platform.runLater {
        clearAll()
        addNorth(new HBox {
            children = Seq(
                new Label { text = "Your name " },
                nameField                
            )
        })
        addCenter(new VBox {
            alignment = Pos.CENTER
            children = 
                (for g <- games if g.joinable yield
                    new VBox {
                        children = Seq(
                            new Label {
                                text = g.name
                            },
                            new Label {
                                text = g.players.mkString(", ")
                            },
                            new Button {
                                text = "Join game"
                                onAction = (_) => {
                                    mgnClient.joinGame(gameServer, g.name, nameField.getText())
                                }
                            }
                        )
                    }
                ) :+ new VBox {
                    children = Seq(
                        new Button {
                            text = "New game"
                            onAction = (_) => {
                                mgnClient.newGame(gameServer, nameField.getText())
                            }
                        }
                    )
                }
        })
    }


}