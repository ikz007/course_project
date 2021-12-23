package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax._
import lv.scala.aml.common.dto.responses.{ ErrorType, HttpResponse}
import lv.scala.aml.common.dto.{Account, IBAN}
import lv.scala.aml.database.repository.interpreter.AccountRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class AccountService[F[_]: Sync](
  accountInterpreter: AccountRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Account]] = accountInterpreter.get
  private def getById(iban: IBAN): OptionT[F, Account] = accountInterpreter.getById(iban)
  private def update(account: Account): F[Unit] = accountInterpreter.update(account)

  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "accounts" / "all" =>
      get.flatMap { case list => Ok( HttpResponse(true, list))
      case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "accounts" / id =>
      getById(IBAN(id)).value.flatMap {
        case Some(account) => Ok(HttpResponse(true, account))
        case None => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case req @ PUT -> Root / "accounts" =>
      req.decode[Account] { acc =>
        update(acc).flatMap(_ => Accepted(HttpResponse(true, ())))
      }.handleErrorWith(_ => BadRequest(HttpResponse(false, ErrorType.UpdateFailed.value)))
  }
}

object AccountService {
  def apply[F[_]: Sync](
    accountRepositoryInterpreter: AccountRepositoryInterpreter[F]
  ): AccountService[F] = new AccountService[F](accountRepositoryInterpreter)
}