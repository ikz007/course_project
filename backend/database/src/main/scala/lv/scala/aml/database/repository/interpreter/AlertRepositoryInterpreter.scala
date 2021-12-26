package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.util.meta.Meta

import java.time.{Instant, LocalDate}
import java.util.Date
import doobie.implicits.javasql._
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.quill.DoobieContext.MySQL
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Alert, IBAN, Relationship}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.AlertRepository

class AlertRepositoryInterpreter[F[_]: Sync](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends AlertRepository[F] with Schema {
  import ctx._
  override def get: F[List[Alert]] =  run(quote {
    query[Alert]
  }).transact(xa)

  override def getById(id: Int): OptionT[F, Alert] =
    OptionT(run(quote {
      query[Alert].filter(_.AlertId == lift(id))
    }).transact(xa).map(_.headOption))

  override def getCustomerAlerts(customerID: String): F[List[Alert]] =
    run(quote {
      query[Alert]
        .join(query[Relationship])
        .on(_.Iban == _.IBAN)
        .filter(_._2.CustomerID == lift(customerID))
        .map{ case (alert, _) => alert}
    }).transact(xa)

  override def getAccountAlerts(Iban: IBAN): F[List[Alert]] =
    run(quote {
      query[Alert]
        .filter(alert => alert.Iban == lift(Iban))
    }).transact(xa)

  override def getTransactionAlerts(reference: String): F[List[Alert]] =
    run(quote {
            query[Alert]
              .filter(alert => alert.TransactionReferences == lift(reference))
          }).transact(xa)
}
object AlertRepositoryInterpreter {

}