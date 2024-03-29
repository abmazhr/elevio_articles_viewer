name := "scala-zio-asynchronous-version"

version := "0.1"

scalaVersion := "2.12.8"

val zioVersion       = "1.0.0-RC8-12"
val sttpVersion      = "1.6.0"
val circeVersion     = "0.10.0"
val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "dev.zio"               %% "zio"           % zioVersion,
  "com.softwaremill.sttp" %% "core"          % sttpVersion,
  "io.circe"              %% "circe-core"    % circeVersion,
  "io.circe"              %% "circe-generic" % circeVersion,
  "io.circe"              %% "circe-parser"  % circeVersion,
  "org.scalactic"         %% "scalactic"     % scalaTestVersion,
  "org.scalatest"         %% "scalatest"     % scalaTestVersion % "test"
)
