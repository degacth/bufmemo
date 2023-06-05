package app.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object BufferManagerActor:
  private lazy val TAG: String = getClass.getSimpleName

  sealed trait BufferMessage
  case class NewBuffer(value: String) extends BufferMessage

  def apply(manager: app.buffer.BufferManager, receiver: ActorRef[Any]): Behavior[BufferMessage] =
    Behaviors.setup { ctx =>
      manager.onChanged { content =>
        ctx.self ! NewBuffer(content)
      }

      Behaviors.receiveMessage {
        case m@NewBuffer(v) =>
          ctx.log.info(s"$TAG got message $v")
          receiver ! m
          Behaviors.same
      }
    }
