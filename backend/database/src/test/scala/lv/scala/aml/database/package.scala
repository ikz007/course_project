package lv.scala.aml

import cats.effect.{Async, ContextShift, IO, Sync}
import cats.implicits._
import doobie.hikari.HikariTransactor
import lv.scala.aml.config.Config

import scala.concurrent.ExecutionContext

package object database {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  def getTransactor[F[_]: Sync : Async : ContextShift]: F[HikariTransactor[F]] =
      for {
      config <- Config.load[F]
      xa <- Sync[F].pure(Database.buildTransactor[F](Database.TransactorConfig(config.db)))
    } yield xa

  lazy val testTransactor: HikariTransactor[IO] = getTransactor[IO].unsafeRunSync()
}
