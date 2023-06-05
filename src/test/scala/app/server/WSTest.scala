package app.server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import app.actors.{BufferManagerActor, ConnectionsActor}
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
      object bufferManager extends app.buffer.BufferManager:
        override def onChanged(handler: String => Unit): Unit = ()
        override def update(value: String): Unit = ()

      val connections = testKit.spawn(ConnectionsActor(), "connections")
      val bufferActor = testKit.spawn(BufferManagerActor(bufferManager, connections), "buffer")

      it("should send message to client about it") {
        withWsClient(connections) { client =>
          client.sendMessage(ClientConnected.toStrMsg)
          val textBuffer = "test"
          bufferActor ! app.actors.BufferManagerActor.NewBuffer(textBuffer)
          client.expectMessage(app.server.BufferChanged(textBuffer).toStrMsg)
        }
      }
    }
  }
