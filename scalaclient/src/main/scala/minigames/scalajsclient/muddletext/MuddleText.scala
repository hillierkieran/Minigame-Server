package minigames.scalajsclient.muddletext

import scala.scalajs.js
import org.scalajs.dom
import minigames.scalajsclient.*

object MuddleText extends GameClient {

  // Hold the game metadata
  var metadata:Option[GameMetadata] = None

  var player:String = ""

  /** Sends a command to the server as { "command": command } */
  private def sendCommand(command:String):Unit = 
    for m <- metadata do
        MinigameNetworkClient.send(CommandPackage(m.gameServer, m.name, player, Seq(js.Dynamic.literal(command = command))))


  // Set up the buttons using the DOM api
  val north = dom.document.createElement("button").asInstanceOf[dom.html.Button]
  north.innerText = "NORTH"
  north.onclick = (evt) => sendCommand("NORTH")
  val south = dom.document.createElement("button").asInstanceOf[dom.html.Button]
  south.innerText = "SOUTH"
  south.onclick = (evt) => sendCommand("SOUTH")
  val east = dom.document.createElement("button").asInstanceOf[dom.html.Button]
  east.innerText = "EAST"
  east.onclick = (evt) => sendCommand("EAST")
  val west = dom.document.createElement("button").asInstanceOf[dom.html.Button]
  west.innerText = "WEST"
  west.onclick = (evt) => sendCommand("WEST")

  val userCommand = dom.document.createElement("input").asInstanceOf[dom.html.Input]
  val send = dom.document.createElement("button").asInstanceOf[dom.html.Button]  
  send.innerText = ">"
  send.onclick = (evt) => sendCommand(userCommand.value)

  val textArea = dom.document.createElement("pre")
  textArea.setAttribute("style", "background: black; height: 600px; width: 800px; color: green; margin: 0; padding: 5px;")

  override def load(metadata: GameMetadata, playerName: String): Unit = {
    this.metadata = Some(metadata)
    MainWindow.center.add(textArea)
    for el <- Seq(north, south, east, west, userCommand, send) do MainWindow.south.add(el)
  }

  override def closeGame(): Unit = {
    metadata = None
  }

  override def execute(metadata: GameMetadata, command: js.Dynamic): Unit = {
    this.metadata = Some(metadata)
    command.command.asInstanceOf[String] match {
        case "clearText" =>
            textArea.innerText = ""

        case "appendText" => 
            textArea.innerText += command.text

        case "setDirections" => 
            val directions = command.directions.asInstanceOf[String]
            north.disabled = !directions.contains("N")
            south.disabled = !directions.contains("S")
            east.disabled = !directions.contains("E")
            west.disabled = !directions.contains("W")
    }

  }

}