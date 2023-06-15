package app.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import java.awt.Toolkit
import java.awt.datatransfer.{Clipboard, ClipboardOwner, DataFlavor, FlavorEvent, FlavorListener, Transferable}

import app.actors.model.Domain

object ClipboardActor:
  sealed trait ClipboardMessage
  case class ClipboardChanged(content: Domain.ClipContent) extends ClipboardMessage

  private val clip = Toolkit.getDefaultToolkit.getSystemClipboard

  def apply(receiver: ActorRef[Any], runInit: Boolean = true): Behavior[ClipboardMessage] =
    var latestClip: Domain.ClipContent = Domain.ClipContent("", "")
    def getId: String = java.util.UUID.randomUUID().toString

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
          if !content.isChanged(latestClip) then return ()

          latestClip = content
          ctx.self ! ClipboardChanged(content)

        override def lostOwnership(clipboard: Clipboard, contents: Transferable): Unit = ()

        private def getCopiedData: Domain.ClipContent =
          val content = true match
            case _ if clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor) => getCopiedFiles
            case _ if clip.isDataFlavorAvailable(DataFlavor.stringFlavor) => getCopiedString
            case _ if clip.isDataFlavorAvailable(DataFlavor.imageFlavor) => getCopiedImage
            case _ =>
              ctx.log.warn("UNSUPPORTED DATA FLAVOR")
              "<<< UNSUPPORTED DATA FLAVOR >>>"

          Domain.ClipContent(id = getId, content = content)

        private def getCopiedFiles: String = clip.getData(DataFlavor.javaFileListFlavor).toString
        private def getCopiedString: String = clip.getData(DataFlavor.stringFlavor).toString
        private def getCopiedImage: String = clip.getData(DataFlavor.imageFlavor).toString

      if runInit then ClipboardListener.init()

      Behaviors.logMessages(Behaviors.receiveMessage {
        case m: ClipboardChanged =>
          receiver ! m
          Behaviors.same
      })
    }
