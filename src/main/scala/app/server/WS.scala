package app.server

import akka.actor.Status.Success
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.impl.engine.HttpIdleTimeoutException
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{AbruptStageTerminationException, CompletionStrategy, FlowShape, Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.typed.scaladsl.ActorSink
import akka.stream.typed.scaladsl.ActorSource

class WS()(implicit as: ActorSystem[_]) extends Directives:
  private val TAG = getClass.getSimpleName

  def makeConnectionHandler(source: Source[SocketMessage, ActorRef[SocketMessage]],
                            connections: ActorRef[Any]): Flow[Message, Message, Any] =

    val clientId = java.util.UUID.randomUUID().toString
    as.log.info(s"$TAG create connection for client with ID $clientId")

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
          case _: AbruptStageTerminationException => as.log.warn(s"$TAG sink abrupt")
          case _: HttpIdleTimeoutException =>
            ClientLeave(clientId)
          case e: Throwable =>
            as.log.warn(s"$TAG sink error ${e.toString}")
            ClientLeave(clientId)
        })

        mat ~> merge ~> sink
        incomingMessage ~> fromClient ~> merge
        connection ~> outgoingMessage
        FlowShape(incomingMessage.in, outgoingMessage.out)
    }

  def makeConnectionSource: Source[SocketMessage, ActorRef[SocketMessage]] = ActorSource.actorRef[SocketMessage](
    completionMatcher = {
      case StopMessages =>
        as.log.info(s"$TAG source completed")
        CompletionStrategy.immediately
    },
    failureMatcher = {
      case e: Throwable =>
        as.log.warn(s"$TAG source error $e")
        Exception(s"connection source exception $e")
    },
    bufferSize = 8,
    overflowStrategy = OverflowStrategy.fail
  )
