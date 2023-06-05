package app.server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import app.actors.BufferManagerActor
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

class WSTest extends WsSpecBase:

  import app.server.SocketMessagesOpts._

  describe("WebSocket tests") {
    describe("When client connected") {
      it("should send connected message") {
        withWsClient(testKit.spawn(Behaviors.empty[Any])) { client =>
          isWebSocketUpgrade shouldBe true
        }
      }
    }

    describe("When clip changed") {
      object Connections:
        def apply(conns: Map[String, ActorRef[SocketMessage]] = Map.empty): Behavior[Any] = Behaviors.setup { ctx =>
          Behaviors.receiveMessage {
            case ClientJoined(id, client) =>
              apply(conns + (id -> client))
            case app.actors.BufferManagerActor.BufferChanged(value) =>
              conns.values.foreach { ref =>
                ref ! app.server.BufferChanged(value)
              }
              Behaviors.same
          }
        }

      object bufferManager extends app.buffer.BufferManager:
        override def onChanged(handler: String => Unit): Unit = ()
        override def update(value: String): Unit = ()

      val connections = testKit.spawn(Connections(), "connections")
      val bufferActor = testKit.spawn(BufferManagerActor(bufferManager, connections), "buffer")

      it("should send message to client about it") {
        withWsClient(connections) { client =>
          client.sendMessage("just to init client")
          val textBuffer = "test"
          bufferActor ! app.actors.BufferManagerActor.BufferChanged(textBuffer)
          client.expectMessage(app.server.BufferChanged(textBuffer).toStrMsg)
        }
      }
    }
  }
