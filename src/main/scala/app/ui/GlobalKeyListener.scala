package app.ui

import com.github.kwhat.jnativehook.{GlobalScreen, NativeHookException}
import com.github.kwhat.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}

import scala.collection.mutable

case class GlobalKeyListener(hotKeys: Map[String, GlobalKeyListener.HotKeys]):
  private val allHotKeys = hotKeys.values.flatMap(_.keys).toSet[Int]
  private val pressedKeys: mutable.Map[Int, Boolean] = mutable.Map() ++ allHotKeys.map(_ -> false).toMap

  def init(): () => Unit =
    try
      GlobalScreen.registerNativeHook()
    catch
      case ex: NativeHookException => System.err.println(
        s"There was a problem registering the native hook: ${ex.getMessage}"
      )

    val nativeKeyListener = new NativeKeyListener:
      override def nativeKeyPressed(nativeKeyEvent: NativeKeyEvent): Unit =
        if !isInHotKeys(nativeKeyEvent.getKeyCode) then return ()

        pressedKeys(nativeKeyEvent.getKeyCode) = true
        pressedAction.foreach(_())

      override def nativeKeyReleased(nativeKeyEvent: NativeKeyEvent): Unit =
        if !isInHotKeys(nativeKeyEvent.getKeyCode) then return ()

        pressedKeys(nativeKeyEvent.getKeyCode) = false

      private def isInHotKeys(code: Int): Boolean = allHotKeys.contains(code)

      private def pressedAction: Option[() => Unit] =
        val pressed = pressedKeys.filter((_, v) => v).keys.toList
        hotKeys.values.find(hotkey => hotkey.keys.forall(pressed.contains(_))).map(_.handler)

    GlobalScreen.addNativeKeyListener(nativeKeyListener)

    () =>
      try
        GlobalScreen.unregisterNativeHook()
        GlobalScreen.removeNativeKeyListener(nativeKeyListener)
      catch {
        case e: Exception => e.printStackTrace()
      }

object GlobalKeyListener:
  case class HotKeys(keys: List[Int], handler: () => Unit)
