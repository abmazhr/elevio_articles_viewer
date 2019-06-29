name := "scala-normal-synchronous-version"

version := "0.1"

scalaVersion := "2.12.8"

val sttpVersion      = "1.6.0"
val circeVersion     = "0.10.0"
val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp" %% "core"          % sttpVersion,
  "io.circe"              %% "circe-core"    % circeVersion,
  "io.circe"              %% "circe-generic" % circeVersion,
  "io.circe"              %% "circe-parser"  % circeVersion,
  "org.scalactic"         %% "scalactic"     % scalaTestVersion,
  "org.scalatest"         %% "scalatest"     % scalaTestVersion % "test"
)
