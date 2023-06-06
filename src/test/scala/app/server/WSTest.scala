package app.server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import app.actors.{ClipboardActor, ConnectionsActor}
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
      val connections = testKit.spawn(ConnectionsActor(), "connections")
      val mediator = testKit.spawn(Behaviors.receiveMessage {
        case ClipboardActor.ClipboardChanged(content) =>
          connections ! WsClipboardChanged(content.toString)
          Behaviors.same
      }, "mediator")
      val clipboard = testKit.spawn(ClipboardActor(mediator, false), "clipboard")

      it("should send message to client about it") {
        withWsClient(connections) { client =>
          client.sendMessage(ClientConnected.toStrMsg)
          val textBuffer = "test"
          clipboard ! ClipboardActor.ClipboardChanged(textBuffer)
          client.expectMessage(app.server.WsClipboardChanged(textBuffer).toStrMsg)
        }
      }
    }
  }
