package minigames.commands

import minigames.rendering.GameMetadata
import io.vertx.core.json.JsonObject

case class RenderingPackage(metadata:GameMetadata, renderingCommands:Seq[JsonObject])