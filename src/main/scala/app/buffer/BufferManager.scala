package app.buffer

trait BufferManager:
  def onChanged(handler: String => Unit): Unit
  def update(value: String): Unit
