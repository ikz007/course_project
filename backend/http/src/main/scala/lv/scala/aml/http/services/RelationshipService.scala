package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.{Account, Customer, IBAN, Relationship}
import lv.scala.aml.database.repository.interpreter.RelationshipRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl

class RelationshipService[F[_]: Sync](
  relationshipRepositoryInterpreter: RelationshipRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Relationship]] = relationshipRepositoryInterpreter.get
  private def getById(relID: String): OptionT[F, Relationship] = relationshipRepositoryInterpreter.getById(relID)
  private def update(rel: Relationship): F[Unit] = relationshipRepositoryInterpreter.update(rel)
  private def getRelatedAccounts(customerID: String): F[List[Account]] = relationshipRepositoryInterpreter.getRelatedAccounts(customerID)
  private def getRelatedCustomers(IBAN: IBAN): F[List[Customer]] = relationshipRepositoryInterpreter.getRelatedCustomers(IBAN)

  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "relationships" / "all" =>
      get.flatMap { case list => Ok(list.asJson)
      case Nil => NotFound()
      }
    case _ @ GET -> Root / "relationships" / id =>
      getById(id).value.flatMap {
        case Some(relationship) => Ok(relationship.asJson)
        case None => NotFound("Such account does not exist")
      }
    case _ @ GET -> Root / "relationships" / "customer" / customerID =>
      getRelatedAccounts(customerID).flatMap {
        case list => Ok(list.asJson)
        case Nil => NotFound()
      }
    case _ @ GET -> Root / "relationships" / "account" / iban =>
      getRelatedCustomers(IBAN(iban)).flatMap {
        case list => Ok(list.asJson)
        case Nil => NotFound()
      }
    case req @ PUT -> Root / "relationships" =>
      req.decode[Relationship] { acc =>
        update(acc).flatMap(_ => Accepted())
      }.handleErrorWith(e => BadRequest(e.getMessage))
  }
}

object RelationshipService {
  def apply[F[_]: Sync](
    relationshipRepositoryInterpreter: RelationshipRepositoryInterpreter[F]
    ): RelationshipService[F] = new RelationshipService[F](relationshipRepositoryInterpreter)
}
