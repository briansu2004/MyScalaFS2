ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "fs2-infinite-stream-simple"
  )


// FS2 v3.x supports Cats Effect 3 (current max version is 3.2.5)
// FS2 v2.x supports Cats Effect 2 (max version is v2.5.10)
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

libraryDependencies += "co.fs2" %% "fs2-core" % "2.5.10"
//libraryDependencies += "co.fs2" %% "fs2-core" % "3.2.5"
//libraryDependencies += "co.fs2" %% "fs2-io" % "3.2.5" // Node.js only
//libraryDependencies += "co.fs2" %% "fs2-scodec" % "3.2.5"
