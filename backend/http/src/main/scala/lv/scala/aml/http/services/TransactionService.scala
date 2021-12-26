package lv.scala.aml.http.services
import cats.data.{OptionT, Validated}
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.responses.ErrorType.{BusinessObjectNotFound, FailedToParse}
import lv.scala.aml.common.dto.responses.{ErrorType, HttpResponse}
import lv.scala.aml.common.dto.{IBAN, IBANHandler, Transaction}
import lv.scala.aml.database.repository.interpreter.TransactionRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
class TransactionService[F[_]: Sync](
  transactionRepositoryInterpreter: TransactionRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Transaction]] = transactionRepositoryInterpreter.get
  private def getById(reference: String): OptionT[F, Transaction] = transactionRepositoryInterpreter.getById(reference)
  private def getQuestTransactions(questID: String): F[List[Transaction]] = transactionRepositoryInterpreter.getQuestTransactions(questID)
  private def getCustomerTransactions(customerID: String): F[List[Transaction]] = transactionRepositoryInterpreter.getCustomerTransactions(customerID)
  private def getAccountTransactions(iban: IBAN): F[List[Transaction]] = transactionRepositoryInterpreter.getAccountTransactions(iban)
  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "transactions" / "all" =>
      get.flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "transactions" / "customer" / customerID =>
      getCustomerTransactions(customerID).flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "transactions" / "account" / iban =>
      IBANHandler.validate(iban) match {
        case Validated.Valid(id) =>  getAccountTransactions(id).flatMap {
          case list => Ok(HttpResponse(true, list.asJson))
          case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
        }
        case Validated.Invalid(_) => BadRequest(HttpResponse(false, FailedToParse.value))
      }

    case _ @ GET -> Root / "transactions" / "quest" / id =>
      getQuestTransactions(id).flatMap { case list => Ok(HttpResponse(true, list.asJson))
      case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "transactions" / id =>
      getById(id).value.flatMap {
        case Some(transaction) => Ok(HttpResponse(true, transaction.asJson))
        case None => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
  }
}

object TransactionService {
  def apply[F[_]: Sync](
    transactionRepositoryInterpreter: TransactionRepositoryInterpreter[F]
  ): TransactionService[F] = new TransactionService[F](transactionRepositoryInterpreter)
}