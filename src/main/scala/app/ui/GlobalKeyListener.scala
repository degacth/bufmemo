package app.ui

import com.github.kwhat.jnativehook.{GlobalScreen, NativeHookException}
import com.github.kwhat.jnativehook.keyboard.{NativeKeyAdapter, NativeKeyEvent, NativeKeyListener}

import scala.collection.mutable

case class GlobalKeyListener():
  private val hotKeys = List(NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SEMICOLON)
  private val pressedHotKeys: mutable.Map[Int, Boolean] = mutable.Map(hotKeys.map(key => key -> false).toMap.toSeq: _*)

  def init(f: => Unit): () => Unit =
    try
      GlobalScreen.registerNativeHook()
    catch
      case ex: NativeHookException => System.err.println(
        s"There was a problem registering the native hook: ${ex.getMessage}"
      )

    val nativeKeyListener = new NativeKeyListener:
      override def nativeKeyPressed(nativeKeyEvent: NativeKeyEvent): Unit = 
        if !isInHotKeys(nativeKeyEvent.getKeyCode) then return ()
        setHotKeyPressed(nativeKeyEvent.getKeyCode, value = true)
        if (isAllKeyPressed) f

      override def nativeKeyReleased(nativeKeyEvent: NativeKeyEvent): Unit = 
        if !isInHotKeys(nativeKeyEvent.getKeyCode) then return ()
        setHotKeyPressed(nativeKeyEvent.getKeyCode, value = false)

      private def isInHotKeys(code: Int): Boolean = hotKeys.contains(code)

      private def setHotKeyPressed(code: Int, value: Boolean): Unit = pressedHotKeys(code) = value

      private def isAllKeyPressed: Boolean = pressedHotKeys.values.forall(v => v)

    GlobalScreen.addNativeKeyListener(nativeKeyListener)

    () =>
      try
        GlobalScreen.unregisterNativeHook()
        GlobalScreen.removeNativeKeyListener(nativeKeyListener)
      catch {
        case e: Exception => e.printStackTrace()
      }
