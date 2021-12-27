package lv.scala.aml.http.services

import cats.data.{OptionT, Validated}
import cats.effect.Sync
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.circe.syntax._
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Account.{accountDecoder, accountEncoder}
import lv.scala.aml.common.dto.responses.ErrorType._
import lv.scala.aml.common.dto.responses.HttpResponse
import lv.scala.aml.common.dto.{Account, IBAN, IBANHandler}
import lv.scala.aml.database.repository.interpreter.AccountRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class AccountService[F[_]: Sync : Logger](
  accountInterpreter: AccountRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Account]] = accountInterpreter.get
  private def getById(iban: IBAN): OptionT[F, Account] = accountInterpreter.getById(iban)
  private def update(account: Account): F[Unit] = accountInterpreter.update(account)

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "accounts" / "all" =>
      get.flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "accounts" / id =>
      IBANHandler.validate(id) match {
        case Validated.Valid(id) => getById(id).value.flatMap {
          case Some(account) => Ok(HttpResponse(true, account.asJson))
          case None => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
        }
        case Validated.Invalid(_) => BadRequest(HttpResponse(false, FailedToParse.value))
      }
    case req @ PUT -> Root / "accounts" =>
      req.decode[Account] { acc =>
        update(acc).flatMap(_ => Accepted(HttpResponse(true, ())))
      }.handleErrorWith(_ => BadRequest(HttpResponse(false, UpdateFailed.value)))
  }
}

object AccountService {
  def apply[F[_]: Sync: Logger](
    transactor: HikariTransactor[F],
    ctx: MySQL[CamelCase] with Decoders with Encoders
  ): AccountService[F] = {
    val repository = new AccountRepositoryInterpreter[F](transactor, ctx)
    new AccountService[F](repository)
  }
}