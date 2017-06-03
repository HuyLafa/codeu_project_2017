name := """chat-app"""

version := "2.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.webjars" % "flot" % "0.8.3",
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  evolutions,
  "com.typesafe.play" %% "play-json" % "2.5.10",
  javaJdbc % Test,
  "org.asynchttpclient" % "async-http-client" % "2.0.31",
  "org.assertj" % "assertj-core" % "3.6.2" % Test,
  "org.awaitility" % "awaitility" % "2.0.0" % Test
)

fork in run := false

// Needed to make JUnit report the tests being run
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))