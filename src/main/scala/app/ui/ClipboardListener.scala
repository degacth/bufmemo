package app.ui

import java.awt.Toolkit
import java.awt.datatransfer.{Clipboard, ClipboardOwner, DataFlavor, FlavorEvent, FlavorListener, Transferable}

class ClipboardListener extends FlavorListener with ClipboardOwner:
  private val clip = Toolkit.getDefaultToolkit.getSystemClipboard
  private var latestClipboard: Any = ""

  clip.setContents(clip.getContents(null), this)
  clip.addFlavorListener(this)
  try Thread.sleep(1000)
  catch
    case e: InterruptedException => println(s"${e.getMessage}")

  override def flavorsChanged(e: FlavorEvent): Unit =
    clip.removeFlavorListener(this)
    clip.setContents(clip.getContents(null), this)
    clip.addFlavorListener(this)

    // todo copied images never changes path
    val content = getCopiedData
    if content == latestClipboard then return ()

    latestClipboard = content
    println("clipboard changed " + content)

  override def lostOwnership(clipboard: Clipboard, contents: Transferable): Unit = ()

  private def getCopiedData: Any =
    if clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor) then return getCopiedFiles
    if clip.isDataFlavorAvailable(DataFlavor.stringFlavor) then return getCopiedString
    if clip.isDataFlavorAvailable(DataFlavor.imageFlavor) then return getCopiedImage
    println("Unsupported data flavor")

  private def getCopiedFiles: Any = clip.getData(DataFlavor.javaFileListFlavor)

  private def getCopiedString: Any = clip.getData(DataFlavor.stringFlavor)

  private def getCopiedImage: Any = clip.getData(DataFlavor.imageFlavor)

// https://cooltrickshome.blogspot.com/2016/11/access-clipboard-content-using-java.html