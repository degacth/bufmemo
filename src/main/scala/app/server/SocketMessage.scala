package app.server

import akka.actor.typed.ActorRef
import app.actors.model.Domain

sealed trait WsConnectionMessage
case class ClientJoined(id: String, ref: ActorRef[SocketMessage]) extends WsConnectionMessage
case class ClientLeave(id: String) extends WsConnectionMessage
case class ClientMessage(clientId: String, msg: SocketMessage)

sealed trait SocketMessage:
  val payload: Any

sealed trait EmptySocketMessage extends SocketMessage:
  override val payload: Any = null

case object ClientConnected extends EmptySocketMessage
case class WsClipboardChanged(payload: Domain.ClipContent) extends SocketMessage
case object StopMessages extends EmptySocketMessage
case class ParseErrorSocketMessage(payload: String) extends SocketMessage
case object WsGetClips extends EmptySocketMessage
case class WsGotClips(payload: List[Domain.ClipContent]) extends SocketMessage

object SocketMessagesOpts:
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  implicit class MessageToString(msg: SocketMessage):
    def toStrMsg: String = msg.asJson.noSpaces

  implicit class MessageFromString(msg: String):
    def fromStringMessage: Either[Error, SocketMessage] = decode[SocketMessage](msg)
