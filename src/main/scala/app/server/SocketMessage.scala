package app.server

import akka.actor.typed.ActorRef


sealed trait SocketMessage:
  val payload: Any

case class ClientJoined(id: String, ref: ActorRef[SocketMessage])
case class ClientLeave(id: String)
case class ClientMessage(clientId: String, msg: SocketMessage)

sealed trait EmptySocketMessage extends SocketMessage:
  override val payload: Any = null

case object ClientConnected extends EmptySocketMessage
case class BufferChanged(payload: String) extends SocketMessage
case object StopMessages extends EmptySocketMessage
case class ParseErrorSocketMessage(payload: String) extends SocketMessage

object SocketMessagesOpts:
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  implicit class MessageToString(msg: SocketMessage):
    def toStrMsg: String = msg.asJson.noSpaces

  implicit class MessageFromString(msg: String):
    def fromStringMessage: Either[Error, SocketMessage] = decode[SocketMessage](msg)
