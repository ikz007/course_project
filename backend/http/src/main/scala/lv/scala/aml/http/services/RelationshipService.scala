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
import lv.scala.aml.common.dto.responses.{ErrorType, HttpResponse}
import lv.scala.aml.common.dto.{Account, Customer, IBAN, Relationship}
import lv.scala.aml.database.repository.interpreter.RelationshipRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl

class RelationshipService[F[_]: Sync : Logger](
  relationshipRepositoryInterpreter: RelationshipRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Relationship]] = relationshipRepositoryInterpreter.get
  private def getById(relID: String): OptionT[F, Relationship] = relationshipRepositoryInterpreter.getById(relID)
  private def update(rel: Relationship): F[Unit] = relationshipRepositoryInterpreter.update(rel)
  private def getRelatedAccounts(customerID: String): F[List[Account]] = relationshipRepositoryInterpreter.getRelatedAccounts(customerID)
  private def getRelatedCustomers(IBAN: IBAN): F[List[Customer]] = relationshipRepositoryInterpreter.getRelatedCustomers(IBAN)

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "relationships" / "all" =>
      get.flatMap { case list => Ok(HttpResponse(true, list.asJson))
      case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "relationships" / id =>
      getById(id).value.flatMap {
        case Some(relationship) => Ok(HttpResponse(true, relationship.asJson))
        case None => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "relationships" / "customer" / customerID =>
      getRelatedAccounts(customerID).flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "relationships" / "account" / iban =>
      getRelatedCustomers(IBAN(iban)).flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case req @ PUT -> Root / "relationships" =>
      req.decode[Relationship] { acc =>
        update(acc).flatMap(_ => Accepted(HttpResponse(true, ())))
      }.handleErrorWith(_ => BadRequest(HttpResponse(false, ErrorType.UpdateFailed.value)))
  }
}

object RelationshipService {
  def apply[F[_]: Sync: Logger](
    transactor: HikariTransactor[F],
    ctx: MySQL[CamelCase] with Decoders with Encoders
  ): RelationshipService[F] = {
    val repository = new RelationshipRepositoryInterpreter[F](transactor, ctx)
    new RelationshipService[F](repository)
  }
}
