ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "fs2_big_csv_simple"
  )

//libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
//libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

libraryDependencies += "co.fs2" %% "fs2-core" % "2.2.1"
libraryDependencies += "co.fs2" %% "fs2-io" % "2.2.1"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  // circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  //"io.circe" %% "circe-parser" % circeVersion,
)