package lv.scala.aml

import cats.effect.IO
import doobie.hikari.HikariTransactor
import lv.scala.aml.config.Config
import lv.scala.aml.database.{Database, DbInit}

import scala.concurrent.ExecutionContext

package object http {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  def getTransactor: IO[HikariTransactor[IO]] =
    for {
      config <- Config.load[IO]
      xa <- IO.pure(Database.buildTransactor[IO](Database.TransactorConfig(config.db)))
      _ <- DbInit.initialize(xa)
    } yield xa

  lazy val testTransactor: HikariTransactor[IO] = getTransactor.unsafeRunSync()
}
