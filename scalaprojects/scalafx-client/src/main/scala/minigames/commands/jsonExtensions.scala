package minigames.commands

import io.vertx.core.json.*
import scala.jdk.StreamConverters.*
import com.fasterxml.jackson.module.scala.ClassTagExtensions
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/** Mapper for Jackson Databind for Scala case classes */
val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build() :: ClassTagExtensions

extension (arr:JsonArray) {

    /** Converts a JsonArray into a Seq[T]. Note that T is just a typecast -- this is usually for JsonArrays that we know will be arrays of JsonObjects */
    def toSeq[T] = arr.stream().toScala(Seq).map(_.asInstanceOf[T])

}