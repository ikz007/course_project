package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import lv.scala.aml.common.dto.{IBAN, Questionnaire, Relationship, Transaction}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.TransactionRepository
import doobie.implicits._
import cats.syntax.all._

class TransactionRepositoryInterpreter [F[_]: Sync](
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

  override def getAccountTransactions(iban: IBAN): F[List[Transaction]] = run(quote {
    query[Transaction]
      .filter(_.OurIBAN == lift(iban))
  }).transact(xa)

  override def get: F[List[Transaction]] = run(quote {
    query[Transaction]
  }).transact(xa)

  override def getById(id: String): OptionT[F, Transaction] =
    OptionT(run(quote {
      query[Transaction].filter(_.Reference == lift(id))
    }).transact(xa).map(_.headOption))

  override def update(model: Transaction): F[Unit] = run(quote {
    query[Transaction]
      .filter(_.Reference == lift(model.Reference))
      .update(lift(model))
  }).transact(xa).void

  override def getQuestTransactions(questId: String): F[List[Transaction]] =
    run(quote {
      query[Transaction]
        .join(query[Relationship]).on(_.OurIBAN == _.IBAN)
        .join(query[Questionnaire]).on{case ((trns, rel), quest) => rel.CustomerID == quest.CustomerID && trns.CountryCode == quest.Country }
        .filter{case ((_, _), quest) => quest.Active && quest.QuestionnaireID == lift(questId)}
        .map{case ((trns, _), _) => trns}
    }).transact(xa)
}