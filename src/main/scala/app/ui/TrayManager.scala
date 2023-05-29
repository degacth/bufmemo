package app.ui

import java.awt.{Image, MenuItem, PopupMenu, SystemTray, TrayIcon}
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, UIManager}

class TrayManager(onExit: => Unit):

  private val tray = SystemTray.getSystemTray
  private val trayIcon = createTrayIcon("/icons/z.gif", "Z-буфер")

  def init(): TrayManager =
    tray.add(trayIcon)

    val popup = new PopupMenu()
    val exitItem = new MenuItem("Exit")
    exitItem.addActionListener(_ => {
      exit()
      onExit
    })

    popup.add(exitItem)
    trayIcon.setPopupMenu(popup)
    this

  def exit(): Unit = tray.remove(trayIcon)

  private def createTrayIcon(path: String, description: String): TrayIcon = {
    val image = ImageIO.read(getClass.getResource(path))
    if (image == null) {
      println(s"Resource not found: $path")
      return null
    }

    val icon = TrayIcon(image)
    val i = ImageIcon(image.getScaledInstance(icon.getSize.width, -1, Image.SCALE_SMOOTH), description)
    TrayIcon(i.getImage)
  }