package app.server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

trait WsSpecBase extends AnyFunSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll:
  protected val testKit: ActorTestKit = ActorTestKit()

  override def afterAll(): Unit = testKit.system.terminate()

  def withWsClient(connections: ActorRef[Any])(body: WSProbe => Unit): Unit = {
    val client = WSProbe()
    val routes = Routes(connections)(testKit.system.asInstanceOf[ActorSystem[Any]]).statics
    WS("/ws", client.flow) ~> routes ~> check {
      body(client)
    }
  }
