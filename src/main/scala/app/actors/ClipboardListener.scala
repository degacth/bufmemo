package app.actors
import akka.actor.typed.ActorRef
import app.actors.model.Domain
import org.slf4j.Logger

import java.awt.Toolkit
import java.awt.datatransfer.*

class ClipboardListener(val log: Logger,
                        changesReceiver: ActorRef[ClipsHolder.Command]) extends FlavorListener with ClipboardOwner:

  private val clip = Toolkit.getDefaultToolkit.getSystemClipboard
  private var latestClip: String = ""

  def init(): Unit =
    clip.setContents(clip.getContents(null), this)
    clip.addFlavorListener(this)
    try Thread.sleep(1000)
    catch case e: InterruptedException => log.warn(s"${e.getMessage}")

  def setClip(content: String): Unit =
    val selection = StringSelection(content)
    clip.setContents(selection, selection)

  override def flavorsChanged(e: FlavorEvent): Unit =
    clip.removeFlavorListener(this)
    clip.setContents(clip.getContents(null), this)
    clip.addFlavorListener(this)

    // TODO copied images never changes path
    val content = getCopiedData
    if content.isBlank then return ()
    if content == latestClip then return ()

    latestClip = content
    changesReceiver ! ClipsHolder.AddClipboard {
      Domain.ClipContent(java.util.UUID.randomUUID().toString, content)
    }

  override def lostOwnership(clipboard: Clipboard, contents: Transferable): Unit = ()

  private def getCopiedData: String = true match
    case _ if clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor) => getCopiedFiles
    case _ if clip.isDataFlavorAvailable(DataFlavor.stringFlavor) => getCopiedString
    case _ if clip.isDataFlavorAvailable(DataFlavor.imageFlavor) => getCopiedImage
    case _ =>
      log.warn("UNSUPPORTED DATA FLAVOR")
      "<<< UNSUPPORTED DATA FLAVOR >>>"

  private def getCopiedFiles: String = clip.getData(DataFlavor.javaFileListFlavor).toString
  private def getCopiedString: String = clip.getData(DataFlavor.stringFlavor).toString
  private def getCopiedImage: String = clip.getData(DataFlavor.imageFlavor).toString
