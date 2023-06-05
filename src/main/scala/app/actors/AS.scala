package app.actors
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import app.AppConfig.SystemConfig

class AS(val config: SystemConfig):
  val system: ActorSystem[Any] = ActorSystem[Any](Behaviors.empty, config.name)
