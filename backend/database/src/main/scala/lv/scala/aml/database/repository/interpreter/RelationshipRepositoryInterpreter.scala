package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Account, Customer, IBAN, Relationship}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.RelationshipRepository
import doobie.implicits._
import cats.syntax.all._


class RelationshipRepositoryInterpreter [F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends RelationshipRepository[F] with Schema{
  import ctx._

  override def getRelatedCustomers(iban: IBAN): F[List[Customer]] = run(quote {
    query[Relationship]
      .join(query[Customer])
      .on(_.CustomerID == _.CustomerID)
      .filter(_._1.IBAN == lift(iban))
      .map{ case (_, customer) => customer}
  }).transact(xa)

  override def getRelatedAccounts(customerID: String): F[List[Account]] = run(quote {
    query[Relationship]
      .join(query[Account])
      .on(_.IBAN == _.IBAN)
      .filter(_._1.CustomerID == lift(customerID))
      .map{ case (_, account) => account}
  }).transact(xa)

  override def get: F[List[Relationship]] = run(quote {
    query[Relationship]
  }).transact(xa)

  override def update(model: Relationship): F[Unit] =
    run(quote {
      query[Relationship]
        .filter(_.RelationshipID == lift(model.RelationshipID))
        .update(lift(model))
    }).transact(xa).void

  override def getById(id: String): OptionT[F, Relationship] =
    OptionT(run(quote {
      query[Relationship].filter(_.RelationshipID == lift(id))
    }).transact(xa).map(_.headOption))
}
