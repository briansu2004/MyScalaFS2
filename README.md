# MyScalaFS2

My Scala FS2

## FS2

FS2 is a library for purely functional, effectful, and polymorphic stream processing library in the Scala programming language. Its design goals are compositionality, expressiveness, resource safety, and speed. The name is a modified acronym for Functional Streams for Scala (FSS, or FS2).

FS2 is available for Scala 2.12, Scala 2.13, Scala 3, and Scala.js. FS2 is built upon two major functional libraries for Scala, Cats, and Cats-Effect. Regardless of those dependencies, FS2 core types (streams and pulls) are polymorphic in the effect type (as long as it is compatible with cats-effect typeclasses), and thus FS2 can be used with other effect libraries, such as Monix.

## fs2 sbt

https://fs2.io/#/getstarted/install

The latest version for Cats Effect 3 is 3.2.5, which supports Cats Effect 3 and is cross built for Scala 2.12, 2.13, and 3.0.

The latest version for Cats Effect 2 is 2.5.10, which supports Cats Effect 2 and is similarly cross built for various Scala versions.

But the strange thing is the FS2 release only shows 2.5.9???

https://github.com/typelevel/fs2/releases?page=2

![](image/README/fs2_releases.png)

For Cats Effect 3

```
libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

// available for 2.12, 2.13, 3.0
libraryDependencies += "co.fs2" %% "fs2-core" % "3.2.5"

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "3.2.5"

// optional reactive streams interop
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "3.2.5"

// optional scodec interop
libraryDependencies += "co.fs2" %% "fs2-scodec" % "3.2.5"
```

For Cats Effect 2

```
libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

libraryDependencies += "co.fs2" %% "fs2-core" % "2.5.10"
```

For Scala.js

```
libraryDependencies += "co.fs2" %%% "fs2-core" % "3.2.5"
libraryDependencies += "co.fs2" %%% "fs2-io" % "3.2.5" // Node.js only
libraryDependencies += "co.fs2" %%% "fs2-scodec" % "3.2.5"
```

The following issue will happen if you use FS2 v3.x but with Cats Effect 2.x:

```
Extracting structure failed, reason: not ok build status: Error (BuildMessages(Vector(),Vector(BuildFailure(sbt task failed, see log for details)),Vector(),Vector(),Error))
```

## FS2 stream

![](image/README/fs2_01.png)

![](image/README/fs2_02.png)

![](image/README/fs2_03.png)

![](image/README/fs2_04.png)

![](image/README/fs2_05.png)

![](image/README/fs2_06.png)

```
import fs2._
```

## Pure stream vs Effectful stream vs infinite stream

Pure stream means the stream doesn't have any side effects.

Effectful stream has side effects.

- fs2 pure stream example

```
import fs2._

val pureStream: Stream[Pure, Int] = Stream(1,2,3,4)

pureStream.compile.toList

val a = Stream("John", "Ringo")
val b = Stream("George", "Paul")

(a ++ b).compile.toVector
(a ++ b).compile.toList
(a ++ b).compile.count
```

![](image/README/fs2_pure_stream_example.png)

- fs2 effectful stream example

```
import cats.effect.IO
import fs2._

def getAge(name: String): IO[Int] = IO(name.length)

// start from a pure stream
val stream = Stream("John", "Paul", "George", "Ringo")

// add some effects to it
val effectfulStream = stream.evalMap(getAge)

stream.evalFilter(name => IO(name.contains("o")))
effectfulStream.compile.toList

stream.evalTap(name => IO(println(s"Received: $name"))).compile.drain
```

![](image/README/fs2_effectful_stream_example.png)

- fs2 infinite stream example

```
import fs2._
import cats.effect.{IO, Timer}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

val stream1 = Stream.constant("PING")
stream1.take(20).compile.toList

val stream2 = Stream(1,2,3).repeat
stream2.take(50).compile.toList

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

val stream = Stream.awakeEvery[IO](5.seconds)

stream.take(2).compile.toList
```

![](image/README/fs2_infinite_stream_example.png)

## Big CSV Parse example

![](image/README/fs2_big_csv_parse_sbt.png)

```
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._

import java.io.File

object BigCSVParsing extends IOApp {
  // dataset: https://fr.openfoodfacts.org/data/fr.openfoodfacts.org.products.csv
  val dataset = new File("C:/tmp/fs2_data/data.csv")
  val output = new File("C:/tmp/fs2_data/output.json")

  case class Row(
                  code: String,
                  url: String,
                  creator: String,
                  createdAt: String,
                  createdDate: String,
                  lastModifiedAt: String,
                  lastModifiedDate: String,
                  productName: String,
                  genericName: String,
                  quantity: String
                )

  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use(blocker => {
      fs2.io.file
        .readAll[IO](dataset.toPath, blocker, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .map(_.split("\t"))
        .collect({
          case Array(
            code,
            url,
            creator,
            createdAt,
            createdDate,
            lastModifiedAt,
            lastModifiedDate,
            productName,
            genericName,
            quantity) => Row(
            code,
            url,
            creator,
            createdAt,
            createdDate,
            lastModifiedAt,
            lastModifiedDate,
            productName,
            genericName,
            quantity)
        })
        .filter(_.genericName.nonEmpty)
        .map(_.asJson.noSpaces)
        .through(text.utf8Encode)
        .through(fs2.io.file.writeAll(output.toPath, blocker))
        .compile
        .drain >> IO(println("Done!"))
        // >> same as .flatMap(_ => IO(println("Done!"))
    }).as(ExitCode.Success)
}
```

## Appendix

### sbt for akka

```
val akkaVersion = "2.6.5"
val akkaHttpVersion = "10.2.0"
val akkaHttpJsonSerializersVersion = "1.34.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonSerializersVersion,
  "de.heikoseeberger" %% "akka-http-jackson" % akkaHttpJsonSerializersVersion,
)
```

### sbt for circe

```
val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  // circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
)
```

I have to put these circe imports on the top (above other imports) ???

```
import io.circe.syntax._
import io.circe.generic.auto._
```
