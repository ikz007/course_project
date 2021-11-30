package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.Account
import lv.scala.aml.database.repository.interpreter.AccountRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class AccountService[F[_]: Sync](
  accountInterpreter: AccountRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Account]] = accountInterpreter.get
  private def getById(iban: String): OptionT[F, Account] = accountInterpreter.getById(iban)
  private def update(account: Account): F[String] = accountInterpreter.update(account)

  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "accounts" / "all" =>
      get.flatMap { case list => Ok(list.asJson)
      case Nil => NotFound()
      }
    case _ @ GET -> Root / "accounts" / id =>
      getById(id).value.flatMap {
        case Some(account) => Ok(account.asJson)
        case None => NotFound("Such account does not exist")
      }
    case req @ PUT -> Root / "accounts" =>
      req.decode[Account] { acc =>
        update(acc).flatMap(_ => Accepted())
      }.handleErrorWith(e => BadRequest(e.getMessage))
  }
}

object AccountService {
  def apply[F[_]: Sync](
    accountRepositoryInterpreter: AccountRepositoryInterpreter[F]
  ): AccountService[F] = new AccountService[F](accountRepositoryInterpreter)
}