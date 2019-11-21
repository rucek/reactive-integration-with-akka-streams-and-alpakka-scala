package org.kunicki.reactive_integration

import akka.http.scaladsl.server.{HttpApp, Route}

object HttpServer extends HttpApp {

  private[reactive_integration] val Host = "localhost"
  private[reactive_integration] val Port = 9999
  private[reactive_integration] val Endpoint = "echo"

  override protected def routes: Route = path(Endpoint) {
    post {
      entity(as[String]) { body =>
        println(body)
        complete(body)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    startServer(Host, Port)
  }
}
