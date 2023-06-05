package app.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MainActor:
  def apply(): Behavior[Any] = Behaviors.setup { ctx =>
    val bufferManager = ctx.spawn(BufferManagerActor(???, ???), "buffer-manager")
    Behaviors.same
  }
