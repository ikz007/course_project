package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Account
import lv.scala.aml.database.repository.{AccountRepository, BussinessObjectRepository}
import doobie.implicits._
import cats.syntax.all._
import lv.scala.aml.database.Schema

//why interpreter maybe better call this implementations ?
// why need trait AccountRepository at all ?
class AccountRepositoryInterpreter[F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends BussinessObjectRepository[F, Account] with Schema{
  import ctx._

  def get: F[List[Account]] = run(quote {
    query[Account]
  }).transact(xa)

  def getById(iban: String): OptionT[F, Account] =
    OptionT(run(quote {
      query[Account].filter(_.IBAN == lift(iban))
    }).transact(xa).map(_.headOption))

  def update(account: Account): F[String] = run(quote {
    query[Account]
      .filter(_.IBAN == lift(account.IBAN))
      .update(lift(account))
  }).transact(xa).as(account.IBAN)
}
