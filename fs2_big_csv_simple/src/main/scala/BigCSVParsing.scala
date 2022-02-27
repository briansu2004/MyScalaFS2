import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._

import java.io.File
import java.time._


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
//      val start = LocalDateTime.now()

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
        //.flatMap(_ => IO(println("Done!")))
        // >> IO(println("Done!"))
        // >> same as .flatMap(_ => IO(println("Done!"))

//      val end = LocalDateTime.now()
//      val elapsed = Duration.between(start, end).toMillis
//      println(s"Start:\t\t$start\nEnd:\t\t$end\nElapsed:\t$elapsed milliseconds")
    }).as(ExitCode.Success)
}
