package minigames.muddletext

import io.vertx.core.json.JsonObject
import minigames.scalafxclient.GameClient
import minigames.rendering.GameMetadata
import minigames.scalafxclient.MinigameNetworkClient
import minigames.commands.CommandPackage
import scalafx.scene.control.*
import scalafx.scene.paint.Color
import scalafx.scene.layout.*

class MuddleText() extends GameClient {

  // Hold the game metadata
  // These are kept in options so we can't accidentally send null
  var metadata:Option[GameMetadata] = None
  var client:Option[MinigameNetworkClient] = None
  var player:Option[String] = None

  /** Sends a command to the server as { "command": command } */
  private def sendCommand(command:String):Unit = 
    for 
        c <- client
        m <- metadata 
        p <- player
    do
        c.send(CommandPackage(m.gameServer, m.name, p, Seq(JsonObject().put("command", command))))


  val north = new Button {
    text = "NORTH"
    onAction = { (_) => sendCommand("NORTH") }
  }
    
  val south = new Button {
    text = "SOUTH"
    onAction = { (_) => sendCommand("SOUTH") }
  }
  
  val east = new Button {
    text = "EAST"
    onAction = { (_) => sendCommand("EAST") }
  }
    
  val west = new Button {
    text = "WEST"
    onAction = { (_) => sendCommand("WEST") }
  }

  val userCommand = new TextField {

  }

  val send = new Button {
    text = ">"
    onAction = { (_) => sendCommand(userCommand.getText()) }
  }

  val textArea = new TextArea {
    editable = false
    prefHeight = 600
    prefWidth = 800
    style = "-fx-control-inner-background: black; -fx-text-fill: green;"
  }

  override def load(client:MinigameNetworkClient, metadata: GameMetadata, playerName: String): Unit = {
    this.metadata = Some(metadata)
    this.client = Some(client)
    this.player = Some(playerName)
    client.mainWindow.addCenter(new VBox {
        children = Seq(
            textArea, 
            new HBox {
                children = Seq(north, south, east, west, userCommand, send)
            }
        )
    })
  }

  override def closeGame(): Unit = {
    metadata = None
  }

  override def execute(metadata: GameMetadata, command: JsonObject): Unit = {
    this.metadata = Some(metadata)
    command.getString("command") match {
        case "clearText" =>
            textArea.text = ""

        case "appendText" => 
            textArea.text = textArea.text.value + command.getString("text")

        case "setDirections" => 
            val directions = command.getString("directions")
            north.disable = !directions.contains("N")
            south.disable = !directions.contains("S")
            east.disable = !directions.contains("E")
            west.disable = !directions.contains("W")
    }

  }

}