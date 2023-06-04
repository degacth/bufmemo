package app.server

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer

class Routes(wsActor: ActorRef[Any])(implicit as: ActorSystem[Any]) extends Directives:
  private val ws = WS()

  val statics: Route =
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        getFromResource("static/index.html")
      } ~ {
        getFromResourceDirectory("static")
      }
    } ~ path("ws") {
      handleWebSocketMessages(ws.makeConnectionHandler(ws.makeConnectionSource, wsActor))
    }
