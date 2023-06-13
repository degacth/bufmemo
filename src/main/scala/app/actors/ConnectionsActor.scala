package app.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.server.*

object ConnectionsActor:
  private val TAG = getClass.getSimpleName

  def apply(connections: Map[String, ActorRef[SocketMessage]] = Map.empty): Behavior[Any] = Behaviors.setup { ctx =>
    Behaviors.logMessages {
      Behaviors.receiveMessage {
        case ClientJoined(id, client) => apply(connections + (id -> client))
        case ClientLeave(id) => apply(connections - id)
        case ClientMessage(_, ClientConnected) => Behaviors.same
        case WsClipboardChanged(value) =>
          connections.values.foreach(_ ! WsClipboardChanged(value))
          Behaviors.same

        case ClientMessage(clientId, msg) => msg match
          case m: WsGotClips =>
            connections.get(clientId).foreach(_ ! m)
            Behaviors.same
          case m =>
            ctx.log.warn(s"unhandled client message $m")
            Behaviors.same
      }
    }
  }
