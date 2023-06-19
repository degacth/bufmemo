package app

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig:

  class ClientConfig(conf: Config):
    val address: String = conf.getString("address")

  class SystemConfig(conf: Config):
    class ClipboardConfig(conf: Config):
      val maxItems: Int = conf.getInt("max-items")

    val name: String = conf.getString("name")
    val clipboard: ClipboardConfig = ClipboardConfig(conf.getConfig("clipboard"))

  private val env = sys.env.getOrElse("BUFMEMO_ENV", "prod")
  private val config = ConfigFactory.load(env).withFallback(ConfigFactory.defaultReference())

  lazy val client: ClientConfig = ClientConfig(config.getConfig("app.client"))
  lazy val system: SystemConfig = SystemConfig(config.getConfig("app.system"))
