package org.kunicki.reactive_integration

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import org.kunicki.reactive_integration.CsvImporter._

import scala.concurrent.duration._
import scala.language.postfixOps

case class Model(id: Int, value: String)

object Model {

  def apply(fields: List[ByteString]): Model = Model(fields.head.utf8String.toInt, fields.last.utf8String)
}

class CsvImporter {

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: Materializer = ActorMaterializer()

  val fileBytes: Source[ByteString, NotUsed] = FileTailSource(DataPath, 100, 0, 1 second)
}

object CsvImporter {

  private val DataPath = Paths.get("src/main/resources/data.csv")

  private object Cassandra {
    val Host = "127.0.0.1"
    val Port = 9042
    val InsertQuery = "insert into alpakka.test (id, value) values (now(), ?)"
  }
}
