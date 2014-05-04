name := """hello-play-2_3-scala"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.3.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.webjars" %% "webjars-play" % "2.3-M1",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "requirejs" % "2.1.11-1"
)

fork in (Test,run) := false

lazy val root = (project in file(".")).addPlugins(PlayScala).settings(
  fork in Test := false
)
