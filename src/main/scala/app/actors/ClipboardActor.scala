package app.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import java.awt.Toolkit
import java.awt.datatransfer.{Clipboard, ClipboardOwner, DataFlavor, FlavorEvent, FlavorListener, Transferable}

object ClipboardActor:
  sealed trait ClipboardMessage
  case class ClipboardChanged(content: Any) extends ClipboardMessage

  private val clip = Toolkit.getDefaultToolkit.getSystemClipboard

  def apply(receiver: ActorRef[Any], runInit: Boolean = true): Behavior[ClipboardMessage] =
    var latestClip: Any = ""

    Behaviors.setup { ctx =>
      object ClipboardListener extends FlavorListener with ClipboardOwner:
        def init(): Unit =
          clip.setContents(clip.getContents(null), ClipboardListener)
          clip.addFlavorListener(ClipboardListener)
          try Thread.sleep(1000)
          catch case e: InterruptedException => ctx.log.warn(s"${e.getMessage}")

        override def flavorsChanged(e: FlavorEvent): Unit =
          clip.removeFlavorListener(ClipboardListener)
          clip.setContents(clip.getContents(null), ClipboardListener)
          clip.addFlavorListener(ClipboardListener)

          // todo copied images never changes path
          val content = getCopiedData
          if content == null then return ()
          if content.toString.isBlank then return ()
          if content == latestClip then return ()

          latestClip = content
          ctx.self ! ClipboardChanged(content)

        override def lostOwnership(clipboard: Clipboard, contents: Transferable): Unit = ()

        private def getCopiedData: Any =
          if clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor) then return getCopiedFiles
          if clip.isDataFlavorAvailable(DataFlavor.stringFlavor) then return getCopiedString
          if clip.isDataFlavorAvailable(DataFlavor.imageFlavor) then return getCopiedImage
          ctx.log.warn("Unsupported data flavor")

        private def getCopiedFiles: Any = clip.getData(DataFlavor.javaFileListFlavor)
        private def getCopiedString: Any = clip.getData(DataFlavor.stringFlavor)
        private def getCopiedImage: Any = clip.getData(DataFlavor.imageFlavor)

      if runInit then ClipboardListener.init()

      Behaviors.logMessages(Behaviors.receiveMessage {
        case m: ClipboardChanged =>
          receiver ! m
          Behaviors.same
      })
    }
