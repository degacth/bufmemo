val scala3Version = "3.2.2"

val AkkaVersion = "2.8.2"
val AkkaHttpVersion = "10.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    fork := true,
    name := "bufmemo",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.github.kwhat" % "jnativehook" % "2.2.2",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
    )
  )
