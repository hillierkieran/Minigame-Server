package minigames.rendering

import minigames.rendering.GameMetadata
import scala.util.*
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import io.vertx.core.json.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.node.ObjectNode
import minigames.commands.mapper

import scala.jdk.CollectionConverters.*

case class RenderingPackage(metadata:GameMetadata, renderingCommands:Seq[JsonObject])

object RenderingPackage:
    def fromJson(json:JsonObject) = 
        RenderingPackage(
            GameMetadata.fromJson(json.getJsonObject("metadata")),
            (for 
                case o:JsonObject <- json.getJsonArray("renderingCommands").iterator().asScala
            yield o).toSeq
        );
