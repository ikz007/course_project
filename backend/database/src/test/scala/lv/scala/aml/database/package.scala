package lv.scala.aml

import cats.effect.{Async, ContextShift, IO, Sync}
import cats.implicits._
import doobie.hikari.HikariTransactor
import lv.scala.aml.config.Config

import scala.concurrent.ExecutionContext

package object database {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  def getTransactor: IO[HikariTransactor[IO]] =
      for {
      config <- Config.load[IO]
      xa <- IO.pure(Database.buildTransactor[IO](Database.TransactorConfig(config.db)))
    } yield xa

  lazy val testTransactor: HikariTransactor[IO] = getTransactor.unsafeRunSync()
}
