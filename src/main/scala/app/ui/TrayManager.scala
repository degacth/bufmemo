package app.ui

import java.awt.{Image, MenuItem, PopupMenu, SystemTray, TrayIcon}
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, UIManager}

class TrayManager(onExit: () => Unit):

  private val isTraySupported = SystemTray.isSupported
  private lazy val tray = SystemTray.getSystemTray
  private lazy val trayIcon = createTrayIcon("/icons/z.gif", "Z-буфер")

  def init(): TrayManager =
    if !isTraySupported then return this

    tray.add(trayIcon)

    val popup = new PopupMenu()
    val exitItem = new MenuItem("Exit")
    exitItem.addActionListener(_ => {
      exit()
      onExit()
    })

    popup.add(exitItem)
    trayIcon.setPopupMenu(popup)
    this

  def exit(): Unit = if isTraySupported then tray.remove(trayIcon)

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