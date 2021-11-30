package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{ Country}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.CountryRepository
import doobie.implicits._
import cats.syntax.all._

class CountryRepositoryInterpreter [F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends CountryRepository[F] with Schema{
  import ctx._

  override def get: F[List[Country]] = run(quote {
    query[Country]
  }).transact(xa)

  override def getById(countryISO: String): OptionT[F, Country] =
    OptionT(run(quote {
      query[Country].filter(_.CountryISO == lift(countryISO))
    }).transact(xa).map(_.headOption))

  override def update(country: Country): F[String] =
    run(quote {
      query[Country]
        .filter(_.CountryISO == lift(country.CountryISO))
        .update(lift(country))
    }).transact(xa).as(country.CountryISO)
}
