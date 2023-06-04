package app.server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

class WSTest extends AnyFunSpec with BeforeAndAfterEach with BeforeAndAfterAll with should.Matchers with ScalatestRouteTest:

  import app.server.SocketMessagesOpts._

  private val testKit = ActorTestKit()

  override def afterAll(): Unit = testKit.system.terminate()

  describe("WebSocket tests") {
    describe("When client connected") {
      it("should send connected message") {
        val client = WSProbe()
        object Logger:
          def apply(clients: Map[String, ActorRef[SocketMessage]] = Map.empty): Behavior[Any] = Behaviors.receiveMessage {
            case ClientJoined(id, ref) =>
              apply(clients + (id -> ref))

            case ClientMessage(id, msg) =>
              clients(id) ! msg
              Behaviors.same
          }

        val routes = Routes(testKit.spawn(Logger()))(testKit.system.asInstanceOf[ActorSystem[Any]]).statics

        WS("/ws", client.flow) ~> routes ~> check {
          isWebSocketUpgrade shouldBe true
          val msg = """{"hello": "world"}"""
          client.sendMessage(msg)
          client.expectMessage(ParseErrorSocketMessage(msg).toStrMsg)
        }
      }
    }
  }
