package app.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import app.server.{WsClipboardChanged, WsConnectionMessage}

object MainActor:
  private val TAG = getClass.getSimpleName

  def apply(): Behavior[Any] = Behaviors.setup { ctx =>
    val connectionsActor = ctx.spawn(ConnectionsActor(), "connections")
    val clipboardActor = ctx.spawn(ClipboardActor(ctx.self), "clipboard")

    Behaviors.logMessages {
      Behaviors.receiveMessage {
        case ClipboardActor.ClipboardChanged(content) =>
          connectionsActor ! WsClipboardChanged(content.toString)
          Behaviors.same
        case m: WsConnectionMessage =>
          connectionsActor ! m
          Behaviors.same
        case m =>
          ctx.log.warn(s"$TAG unhandled message $m")
          Behaviors.same
      }
    }
  }
