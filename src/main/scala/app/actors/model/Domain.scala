package app.actors.model
import app.actors.serializers.CborSerializable

object Domain:
  final case class ClipContent(id: String, content: String) extends CborSerializable:
    def isChanged(other: ClipContent): Boolean =
      if content.isBlank then return false
      if content == other.content then return false
      
      true
