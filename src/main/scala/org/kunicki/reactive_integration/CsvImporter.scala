package org.kunicki.reactive_integration

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, GraphDSL, Partition, Sink, Source}
import akka.stream.{ActorMaterializer, Graph, Materializer, SinkShape}
import akka.util.ByteString
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
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

  val toModel: Flow[ByteString, Model, NotUsed] = CsvParsing.lineScanner().map(Model.apply)

  val httpSink: Sink[Model, NotUsed] =
    Flow[Model]
      .map(m => HttpRequest(method = HttpMethods.POST, uri = s"/${HttpServer.Endpoint}", entity = m.value))
      .via(Http().outgoingConnection(HttpServer.Host, HttpServer.Port))
      .to(Sink.ignore)

  val cassandraSink: Sink[Model, NotUsed] = {
    implicit val session: Session = Cluster.builder.addContactPoint(Cassandra.Host).withPort(Cassandra.Port).build.connect
    val preparedStatement = session.prepare(Cassandra.InsertQuery)
    val statementBinder = (m: Model, ps: PreparedStatement) => ps.bind(m.value)

    Flow[Model].to(CassandraSink(1, preparedStatement, statementBinder))
  }

  val partitioningSink: Graph[SinkShape[Model], NotUsed] = GraphDSL.create() { implicit builder =>
    val partition = builder.add(Partition[Model](2, _.id % 2))

    import GraphDSL.Implicits._

    partition ~> cassandraSink
    partition ~> httpSink

    SinkShape(partition.in)
  }
}

object CsvImporter {

  private val DataPath = Paths.get("src/main/resources/data.csv")

  private object Cassandra {
    val Host = "127.0.0.1"
    val Port = 9042
    val InsertQuery = "insert into alpakka.test (id, value) values (now(), ?)"
  }
}
