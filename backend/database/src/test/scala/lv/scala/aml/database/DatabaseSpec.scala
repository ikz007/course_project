package lv.scala.aml.database

import cats.effect.IO
import doobie.hikari.HikariTransactor
import doobie.scalatest.IOChecker
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class DatabaseSpec extends AnyWordSpec with Matchers with TypeCheckedTripleEquals with BeforeAndAfterAll with IOChecker {
  override def transactor: HikariTransactor[IO] = testTransactor

  implicit private val limit: FiniteDuration = 5.seconds

  def timedRun[A](f : IO[A])(implicit time: FiniteDuration): A = {
    f.unsafeRunTimed(time).getOrElse(fail("Unable to complete test"))
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    timedRun(DbInit.initialize[IO](transactor))
  }
}
