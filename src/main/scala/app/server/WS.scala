package app.server

import akka.actor.Status.Success
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{AbruptStageTerminationException, CompletionStrategy, FlowShape, Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.typed.scaladsl.ActorSink
import akka.stream.typed.scaladsl.ActorSource

sealed trait SocketMessage:
  val payload: Any


case class ClientJoined(id: String, ref: ActorRef[SocketMessage])

case class ClientLeave(id: String)

case class ClientMessage(clientId: String, msg: SocketMessage)

sealed trait EmptySocketMessage extends SocketMessage:
  override val payload: Any = null

case object StopMessages extends EmptySocketMessage

case class ParseErrorSocketMessage(payload: String) extends SocketMessage

case object HelloMsg extends EmptySocketMessage // Without any implementation of SocketMessage circe does not works

object SocketMessagesOpts:

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  implicit class MessageToString(msg: SocketMessage):
    def toStrMsg: String = msg.asJson.noSpaces

  implicit class MessageFromString(msg: String):
    def fromStringMessage: Either[Error, SocketMessage] = decode[SocketMessage](msg)

class WS()(implicit as: ActorSystem[Any]) extends Directives:

  def makeConnectionHandler(
                             source: Source[SocketMessage, ActorRef[SocketMessage]],
                             connections: ActorRef[Any]
                           ): Flow[Message, Message, Any] =
    val clientId = java.util.UUID.randomUUID().toString

    Flow fromGraph GraphDSL.createGraph(source) { implicit b =>
      connection =>
        import GraphDSL.Implicits._
        import SocketMessagesOpts._

        val mat = b.materializedValue.map(sock => ClientJoined(clientId, sock))
        val merge = b add Merge[Any](2)
        val incomingMessage: FlowShape[Message, SocketMessage] = b.add(Flow[Message] collect {
          case TextMessage.Strict(msg) => msg.fromStringMessage.getOrElse(ParseErrorSocketMessage(msg))
        })
        val outgoingMessage: FlowShape[SocketMessage, Message] = b.add(Flow[Any] collect {
          case msg: SocketMessage => TextMessage(msg.toStrMsg)
        })
        val fromClient = b.add(Flow[Any] collect {
          case msg: SocketMessage => ClientMessage(clientId, msg)
        })

        val sink = ActorSink.actorRef[Any](connections, ClientLeave(clientId), {
          case _: AbruptStageTerminationException =>
          case e: Throwable => as.log.warn(s"sink error ${e.toString}")
        })

        mat ~> merge ~> sink
        incomingMessage ~> fromClient ~> merge
        connection ~> outgoingMessage
        FlowShape(incomingMessage.in, outgoingMessage.out)
    }

  def makeConnectionSource: Source[SocketMessage, ActorRef[SocketMessage]] = ActorSource.actorRef[SocketMessage](
    completionMatcher = {
      case StopMessages =>
        as.log.info(s"source completed")
        CompletionStrategy.immediately
    },
    failureMatcher = {
      case e: Throwable =>
        as.log.warn(s"source error $e")
        Exception(s"connection source exception $e")
    },
    bufferSize = 8,
    overflowStrategy = OverflowStrategy.fail
  )
