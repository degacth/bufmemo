package app.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object BufferManagerActor:
  private lazy val TAG: String = getClass.getSimpleName

  sealed trait BufferMessage
  case class BufferChanged(value: String) extends BufferMessage

  def apply(manager: app.buffer.BufferManager, receiver: ActorRef[Any]): Behavior[BufferMessage] =
    Behaviors.setup { ctx =>
      manager.onChanged { content =>
        ctx.self ! BufferChanged(content)
      }

      Behaviors.receiveMessage {
        case m@BufferChanged(v) =>
          ctx.log.info(s"$TAG got message $v")
          receiver ! m
          Behaviors.same
      }
    }
