package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.circe.syntax._
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Country
import lv.scala.aml.common.dto.responses.{ErrorType, HttpResponse}
import lv.scala.aml.database.repository.interpreter.CountryRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class CountryService[F[_]: Sync : Logger](
  countryRepositoryInterpreter: CountryRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Country]] = countryRepositoryInterpreter.get
  private def getById(countryISO: String): OptionT[F, Country] = countryRepositoryInterpreter.getById(countryISO)
  private def update(country: Country): F[Unit] = countryRepositoryInterpreter.update(country)

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "countries" / "all" =>
      get.flatMap { case list => Ok(HttpResponse(true, list.asJson))
      case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "countries" / id =>
      getById(id).value.flatMap {
        case Some(account) => Ok(HttpResponse(true, account.asJson))
        case None => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case req @ PUT -> Root / "countries" =>
      req.decode[Country] { acc =>
        update(acc).flatMap(_ => Accepted(HttpResponse(true, ())))
      }.handleErrorWith(_ => BadRequest(HttpResponse(false, ErrorType.UpdateFailed.value)))
  }
}

object CountryService {
  def apply[F[_]: Sync: Logger](
    transactor: HikariTransactor[F],
    ctx: MySQL[CamelCase] with Decoders with Encoders
  ): CountryService[F] = {
    val repository = new CountryRepositoryInterpreter[F](transactor, ctx)
    new CountryService[F](repository)
  }
}