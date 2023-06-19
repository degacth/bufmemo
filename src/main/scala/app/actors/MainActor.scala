package app.actors

import akka.actor.Status.Success
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import app.actors.model.Domain
import app.server.*

object MainActor:
  private val TAG = getClass.getSimpleName

  def apply(): Behavior[Any] = Behaviors.setup { ctx =>

    val connectionsActor = ctx.spawn(ConnectionsActor(), "connections")
    val clipsHolder = ctx.spawn(ClipsHolder(ctx.self), "clips-holder")

    Behaviors.logMessages {
      Behaviors.receiveMessage { (m: Any) =>
        m match
          case ClipsHolder.AddedClip(content) => connectionsActor ! WsClipboardChanged(content)
          case ClipsHolder.CurrentClipUpdated(_, clipId) => connectionsActor ! WsClipboardChanged(Domain.ClipContent(clipId, ""))
          case ClientMessage(clientId, WsSetClip(clipId)) => clipsHolder ! ClipsHolder.UpdateCurrentClip(clientId, clipId)
          case m: WsConnectionMessage => connectionsActor ! m
          case ClientMessage(clientId, WsGetClips) => clipsHolder ! ClipsHolder.GetClips(ctx.self, clientId)
          case ClipsHolder.GotClips(clips, clientId) => connectionsActor ! ClientMessage(clientId, WsGotClips(clips))
          case m => ctx.log.warn(s"$TAG unhandled message ${m.getClass.getCanonicalName}")
        Behaviors.same
      }
    }
  }
