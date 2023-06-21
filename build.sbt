val scala3Version = "3.2.2"

val AkkaVersion = "2.8.2"
val AkkaHttpVersion = "10.5.2"
val circeVersion = "0.14.1"

enablePlugins(JavaAppPackaging)

lazy val root = project
  .in(file("."))
  .settings(
    fork := true,
    name := "bufmemo",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),

    libraryDependencies ++= Seq(
      "com.github.kwhat" % "jnativehook" % "2.2.2",
      "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,

      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
    )
  )
