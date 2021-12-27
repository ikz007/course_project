package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Account, IBAN}
import lv.scala.aml.database.repository.{BussinessObjectRepository, GetByIdRepository}
import doobie.implicits._
import cats.syntax.all._
import lv.scala.aml.database.Schema

class AccountRepositoryInterpreter[F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends BussinessObjectRepository[F, Account] with GetByIdRepository[F, IBAN, Account] with Schema{
  import ctx._

  def get: F[List[Account]] = run(quote {
    query[Account]
  }).transact(xa)

  def getById(iban: IBAN): OptionT[F, Account] =
    OptionT(run(quote {
      query[Account].filter(_.IBAN == lift(iban))
    }).transact(xa).map(_.headOption))

  def update(account: Account): F[Unit] = run(quote {
    query[Account]
      .filter(_.IBAN == lift(account.IBAN))
      .update(lift(account))
  }).transact(xa).void
}
