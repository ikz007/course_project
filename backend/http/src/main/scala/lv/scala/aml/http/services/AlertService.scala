package lv.scala.aml.http.services

import cats.data.{OptionT, Validated}
import cats.effect.{Concurrent, Sync}
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.circe.syntax._
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.responses.ErrorType._
import lv.scala.aml.common.dto.responses.HttpResponse
import lv.scala.aml.common.dto.{Alert, IBAN, IBANHandler}
import lv.scala.aml.database.repository.interpreter.AlertRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

import scala.util.{Failure, Success, Try}

class AlertService[F[_]: Sync : Concurrent : Logger](
  alertRepo: AlertRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Alert]] = alertRepo.get
  private def getTransactionAlerts(reference: String): F[List[Alert]] = alertRepo.getTransactionAlerts(reference)
  private def getById(id: Int): OptionT[F, Alert] = alertRepo.getById(id)
  private def getCustomerAlerts(customerID: String): F[List[Alert]] = alertRepo.getCustomerAlerts(customerID)
  private def getAccountAlerts(iban: IBAN): F[List[Alert]] = alertRepo.getAccountAlerts(iban)

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case _@GET -> Root / "alerts" / "all" =>
        get.flatMap {
          case list => Ok(HttpResponse(true, list.asJson))
          case Nil => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
        }
      case _ @ GET -> Root / "alerts" / id =>
        Try(id.toInt) match {
          case Failure(_) => BadRequest(HttpResponse(false, FailedToParse.value))
          case Success(alertId) => getById(alertId).value.flatMap {
            case Some(alert) => Ok(HttpResponse(true, alert.asJson))
            case None => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
          }
        }
      case GET -> Root / "alerts" / "transaction"/ id =>
        getTransactionAlerts(id).flatMap {
          case list => Ok(HttpResponse(true, list.asJson))
          case Nil => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
        }
      case GET -> Root / "alerts" / "customer"/ customerId =>
        getCustomerAlerts(customerId).flatMap {
          case list => Ok(HttpResponse(true, list.asJson))
          case Nil => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
        }
      case GET -> Root / "alerts" / "account"/ accountId =>
        IBANHandler.validate(accountId) match {
          case Validated.Valid(account) => getAccountAlerts(account).flatMap {
            case list => Ok(HttpResponse(true, list.asJson))
            case Nil => NotFound(HttpResponse(false, BusinessObjectNotFound.value))
          }
          case Validated.Invalid(_) => BadRequest(HttpResponse(false, FailedToParse.value))
        }
    }

}
object AlertService {
  def apply[F[_]: Sync: Logger : Concurrent](
    transactor: HikariTransactor[F],
    ctx: MySQL[CamelCase] with Decoders with Encoders
  ): AlertService[F] = {
    val repository = new AlertRepositoryInterpreter[F](transactor, ctx)
    new AlertService[F](repository)
  }
}