package app.actors

import akka.actor.Status.Success
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import app.server.{WsClipboardChanged, WsConnectionMessage}

object MainActor:
  private val TAG = getClass.getSimpleName

  def apply(): Behavior[Any] = Behaviors.setup { ctx =>
    val connectionsActor = ctx.spawn(ConnectionsActor(), "connections")
    val clipboardActor = ctx.spawn(ClipboardActor(ctx.self), "clipboard")
    val clipsHolder = ctx.spawn(ClipsHolder(), "clips-holder")

    Behaviors.logMessages {
      Behaviors.receiveMessage {
        case ClipsHolder.AddedClip(content) =>
          connectionsActor ! WsClipboardChanged(content)
          Behaviors.same
        case ClipboardActor.ClipboardChanged(content) =>
          clipsHolder ! ClipsHolder.AddClipboard(content.toString, ctx.self)
          Behaviors.same
        case m: WsConnectionMessage =>
          connectionsActor ! m
          Behaviors.same
        case m =>
          ctx.log.warn(s"$TAG unhandled message ${m.getClass.getCanonicalName}")
          Behaviors.same
      }
    }
  }
