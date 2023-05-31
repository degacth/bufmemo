package app.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer

class Routes()(implicit mat: Materializer) extends Directives:
  val statics: Route =
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        getFromResource("static/index.html")
      } ~ {
        getFromResourceDirectory("static")
      }
    } ~ path("echo") {
      handleWebSocketMessages(WSEcho.echo)
    }
