name := """hello-play-2_3-scala"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3-M1",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "requirejs" % "2.1.11-1"
)

fork in (Test,run) := false

lazy val root = (project in file(".")).addPlugins(PlayScala).settings(
  fork in Test := false
)
