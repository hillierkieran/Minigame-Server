package minigames.commands

import io.vertx.core.json.*
import scala.jdk.StreamConverters.*

extension (arr:JsonArray) {

    /** Converts a JsonArray into a Seq[T]. Note that T is just a typecast -- this is usually for JsonArrays that we know will be arrays of JsonObjects */
    def toSeq[T] = arr.stream().toScala(Seq).map(_.asInstanceOf[T])

}