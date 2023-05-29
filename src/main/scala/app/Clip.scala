package app

import java.awt.datatransfer.StringSelection

object Clip:
  private val clipboard = java.awt.Toolkit.getDefaultToolkit.getSystemClipboard

  def setText(text: String): Unit =
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
