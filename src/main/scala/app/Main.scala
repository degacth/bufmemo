package app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import app.actors.MainActor
import app.server.{HttpServer, Routes}
import app.ui.GlobalKeyListener.HotKeys
import app.ui.{GlobalKeyListener, TrayManager}
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

import java.awt.{Desktop, Toolkit}
import java.awt.datatransfer.{Clipboard, ClipboardOwner, DataFlavor, StringSelection, Transferable}
import scala.io.StdIn
import java.net.URI
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

@main def run(): Unit =
  implicit val system: ActorSystem[Any] = ActorSystem(MainActor(), "app-system")
  implicit val ec: ExecutionContextExecutor = system.executionContext

  import akka.http.scaladsl.model._
  import akka.http.scaladsl.server.Directives._

  val address = ("http", "0.0.0.0", 8000)
  val routes = server.Routes(system)

  val httpServer = HttpServer(routes.statics)
  val binding = httpServer.serve(address._2, address._3)

  def stopServer(): Unit = httpServer.unbind(binding).onComplete(_ => system.terminate())

  val exit = () => System.exit(0)

  val stopKeyListener = GlobalKeyListener(Map(
    "openAppUrl" -> HotKeys(List(
      NativeKeyEvent.VC_ALT,
      NativeKeyEvent.VC_CONTROL,
      NativeKeyEvent.VC_SEMICOLON),
      () => if Desktop.isDesktopSupported then Desktop.getDesktop.browse(new URI(AppConfig.client.address))),

    "quitApp" -> HotKeys(List(
      NativeKeyEvent.VC_ALT,
      NativeKeyEvent.VC_CONTROL,
      NativeKeyEvent.VC_CLOSE_BRACKET),
      exit
    )
  )).init()

  TrayManager(exit).init()
