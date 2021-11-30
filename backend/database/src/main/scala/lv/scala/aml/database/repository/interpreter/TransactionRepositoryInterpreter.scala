package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Relationship, Transaction}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.TransactionRepository
import doobie.implicits._
import cats.syntax.all._

class TransactionRepositoryInterpreter [F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends TransactionRepository[F] with Schema{
  import ctx._

  override def getCustomerTransactions(customerID: String): F[List[Transaction]] = run(quote {
    query[Transaction]
      .join(query[Relationship]).on(_.OurIBAN == _.IBAN)
      .filter(_._2.CustomerID == lift(customerID))
      .map{ case (transaction, _) => transaction}
  }).transact(xa)

  override def getAccountTransactions(IBAN: String): F[List[Transaction]] = run(quote {
    query[Transaction]
      .filter(_.OurIBAN == lift(IBAN))
  }).transact(xa)

  override def get: F[List[Transaction]] = run(quote {
    query[Transaction]
  }).transact(xa)

  override def getById(id: String): OptionT[F, Transaction] =
    OptionT(run(quote {
      query[Transaction].filter(_.Reference == lift(id))
    }).transact(xa).map(_.headOption))

  override def update(model: Transaction): F[String] = run(quote {
    query[Transaction]
      .filter(_.Reference == lift(model.Reference))
      .update(lift(model))
  }).transact(xa).as(model.Reference)
}
