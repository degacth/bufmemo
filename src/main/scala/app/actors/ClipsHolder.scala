package app.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import app.actors.serializers.CborSerializable

object ClipsHolder:
  sealed trait Command extends CborSerializable
  final case class AddClipboard(content: String, ref: ActorRef[Event]) extends Command
  final case class GetClips(ref: ActorRef[Event], clientId: String) extends Command

  sealed trait Event extends CborSerializable
  final case class AddedClip(content: String) extends Event
  final case class GotClips(content: List[String], clientId: String) extends Event

  private final case class State(clips: List[String] = Nil)

  def apply(): Behavior[Command] = EventSourcedBehavior.withEnforcedReplies[Command, Event, State](
    PersistenceId.ofUniqueId("clipboards"),
    emptyState = State(),
    commandHandler = commandHandler,
    eventHandler = eventHandler,
  )

  private def commandHandler: (State, Command) => ReplyEffect[Event, State] = (state, command) => command match
    case AddClipboard(content, ref) =>
      val event = AddedClip(content)
      Effect.persist(event).thenReply(ref)(_ => event)
    case GetClips(ref, clientId) => Effect.reply(ref)(GotClips(state.clips, clientId))

  private def eventHandler: (State, Event) => State = (state, event) => event match
    case AddedClip(content) => state.copy(clips = (content :: state.clips).take(10))
    case GotClips(_, _) => state
