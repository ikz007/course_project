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
import lv.scala.aml.common.dto.Alert
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
        .filter(alert => alert.Subject == lift(customerID) && alert.SubjectType ==  lift("Customer"))
    }).transact(xa)

  override def getAccountAlerts(IBAN: String): F[List[Alert]] =
    run(quote {
      query[Alert]
        .filter(alert => alert.Subject == lift(IBAN) && alert.SubjectType ==  lift("Account"))
    }).transact(xa)
  override def getStream: fs2.Stream[F, Alert] =
    sql"select AlertId, Subject, SubjectType, TransactionReferences, AlertedCondition, AlertedValue, cast(DateCreated as date), ScenarioName from Alert"
      .query[Alert]
      .stream
      .transact(xa)
}
object AlertRepositoryInterpreter {

}