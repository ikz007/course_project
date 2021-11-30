package lv.scala.aml.database.repository.interpreter
import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Customer
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.CustomerRepository
import doobie.implicits._
import cats.syntax.all._

class CustomerRepositoryInterpreter[F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends CustomerRepository[F] with Schema{
  import ctx._

  override def get: F[List[Customer]] = run(quote {
    query[Customer]
  }).transact(xa)

  override def getById(customerID: String): OptionT[F, Customer] =
    OptionT(run(quote {
      query[Customer].filter(_.CustomerID == lift(customerID))
    }).transact(xa).map(_.headOption))

  override def update(customer: Customer): F[String] =
    run(quote {
      query[Customer]
        .filter(_.CustomerID == lift(customer.CustomerID))
        .update(lift(customer))
    }).transact(xa).as(customer.CustomerID)
}
