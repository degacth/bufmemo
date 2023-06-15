package app.actors.model
import app.actors.serializers.CborSerializable

object Domain:
  final case class ClipContent(id: String, content: String) extends CborSerializable
