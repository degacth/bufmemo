package app.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.server.{WsClipboardChanged, ClientConnected, ClientJoined, ClientMessage, SocketMessage}

object ConnectionsActor:
  def apply(connections: Map[String, ActorRef[SocketMessage]] = Map.empty): Behavior[Any] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage {
      case ClientJoined(id, client) => apply(connections + (id -> client))
      case ClientMessage(_, ClientConnected) => Behaviors.same
      case WsClipboardChanged(value) =>
        connections.values.foreach(_ ! WsClipboardChanged(value))
        Behaviors.same
    }
  }
