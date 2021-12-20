package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.Country
import lv.scala.aml.database.repository.interpreter.CountryRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class CountryService[F[_]: Sync](
  countryRepositoryInterpreter: CountryRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Country]] = countryRepositoryInterpreter.get
  private def getById(countryISO: String): OptionT[F, Country] = countryRepositoryInterpreter.getById(countryISO)
  private def update(country: Country): F[Unit] = countryRepositoryInterpreter.update(country)

  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "countries" / "all" =>
      get.flatMap { case list => Ok(list.asJson)
      case Nil => NotFound()
      }
    case _ @ GET -> Root / "countries" / id =>
      getById(id).value.flatMap {
        case Some(account) => Ok(account.asJson)
        case None => NotFound("Such account does not exist")
      }
    case req @ PUT -> Root / "countries" =>
      req.decode[Country] { acc =>
        update(acc).flatMap(_ => Accepted())
      }.handleErrorWith(e => BadRequest(e.getMessage))
  }
}

object CountryService {
  def apply[F[_]: Sync](
    countryRepositoryInterpreter: CountryRepositoryInterpreter[F]
  ): CountryService[F] = new CountryService[F](countryRepositoryInterpreter)
}