import fs2._
import cats.effect.{IO, Timer}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

val stream = Stream.awakeEvery[IO](5.seconds)

stream.take(2).compile.toList