package app.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, scaladsl}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import app.actors.serializers.CborSerializable
import app.actors.model.Domain

object ClipsHolder:
  sealed trait Command extends CborSerializable
  final case class AddClipboard(content: Domain.ClipContent) extends Command
  final case class GetClips(ref: ActorRef[Event], clientId: String) extends Command
  final case class UpdateCurrentClip(clientId: String, clipId: String) extends Command

  sealed trait Event extends CborSerializable
  final case class AddedClip(content: Domain.ClipContent) extends Event
  final case class GotClips(content: List[Domain.ClipContent], clientId: String) extends Event
  final case class CurrentClipUpdated(clientId: String, clipId: String) extends Event

  private final case class State(clips: List[Domain.ClipContent] = Nil)

  def apply(changesReceiver: ActorRef[Any]): Behavior[Command] = Behaviors.setup { ctx =>
    def commandHandler: (State, Command) => ReplyEffect[Event, State] = (state, command) => command match
      case AddClipboard(content) =>
        val event = AddedClip(content)
        // TODO update clipboard
        Effect.persist(event).thenReply(changesReceiver)(_ => event)
      case GetClips(ref, clientId) =>
        Effect.reply(ref)(GotClips(state.clips, clientId))
      case UpdateCurrentClip(clientId, clipId) =>
        val event = CurrentClipUpdated(clientId, clipId)
        Effect.persist(event).thenReply(changesReceiver)(_ => event)

    def eventHandler: (State, Event) => State = (state, event) => event match
      case AddedClip(content) => state.copy(clips = (content :: state.clips).take(10))
      case GotClips(_, _) => state
      case m@CurrentClipUpdated(clientId, clipId) =>
        val clips = state.clips.find(_.id == clipId)
          .map(selected =>
            state.clips.span(_ != selected) match
              case (as, h :: bs) => h :: as ++ bs
              case _ => state.clips
          )
          .getOrElse(state.clips)

        state.copy(clips = clips)

    val clipboardListener = ClipboardListener(ctx.log, ctx.self)
    clipboardListener.init()

    Behaviors.logMessages {
      EventSourcedBehavior.withEnforcedReplies[Command, Event, State](
        PersistenceId.ofUniqueId("clipboards"),
        emptyState = State(),
        commandHandler = commandHandler,
        eventHandler = eventHandler,
      )
    }
  }
