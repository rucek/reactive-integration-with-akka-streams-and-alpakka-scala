package org.kunicki.reactive_integration

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString

import scala.language.postfixOps

case class Model(id: Int, value: String)

object Model {

  def apply(fields: List[ByteString]): Model = Model(fields.head.utf8String.toInt, fields.last.utf8String)
}

class CsvImporter {

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: Materializer = ActorMaterializer()

}

object CsvImporter {

  private val DataPath = Paths.get("src/main/resources/data.csv")

  private object Cassandra {
    val Host = "127.0.0.1"
    val Port = 9042
    val InsertQuery = "insert into alpakka.test (id, value) values (now(), ?)"
  }
}
