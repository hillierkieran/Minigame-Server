package minigames.rendering

import minigames.rendering.GameMetadata
import scala.util.*
import minigames.scalafxclient.mapper
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import io.vertx.core.json.*

case class RenderingPackage(metadata:GameMetadata, renderingCommands:Seq[JsonObject])

