package app.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.actors.BufferManagerActor.NewBuffer
import app.server.{BufferChanged, ClientConnected, ClientJoined, ClientMessage, SocketMessage}

object ConnectionsActor:
  def apply(connections: Map[String, ActorRef[SocketMessage]] = Map.empty): Behavior[Any] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage {
      case ClientJoined(id, client) => apply(connections + (id -> client))
      case ClientMessage(_, ClientConnected) => Behaviors.same
      case NewBuffer(value) =>
        connections.values.foreach(_ ! BufferChanged(value))
        Behaviors.same
    }
  }
