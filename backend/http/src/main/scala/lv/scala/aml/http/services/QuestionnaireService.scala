package lv.scala.aml.http.services
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.Questionnaire
import lv.scala.aml.common.dto.responses.{ErrorType, HttpResponse}
import lv.scala.aml.database.repository.interpreter.QuestionnaireRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class QuestionnaireService[F[_]: Sync](
  questionnaireRepositoryInterpreter: QuestionnaireRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Questionnaire]] = questionnaireRepositoryInterpreter.get
  private def getById(id: String): OptionT[F, Questionnaire] = questionnaireRepositoryInterpreter.getById(id)
  private def update(quest: Questionnaire): F[Unit] = questionnaireRepositoryInterpreter.update(quest)
  private def getByCustomerID(customerID: String): F[List[Questionnaire]] = questionnaireRepositoryInterpreter.getByCustomerID(customerID)
  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "quests" / "all" =>
      get.flatMap { case list => Ok(HttpResponse(true, list.asJson))
      case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "quests" / id =>
      getById(id).value.flatMap {
        case Some(quest) => Ok(HttpResponse(true, quest.asJson))
        case None => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case _ @ GET -> Root / "quests" / "customer" / customerID =>
      getByCustomerID(customerID).flatMap {
        case list => Ok(HttpResponse(true, list.asJson))
        case Nil => NotFound(HttpResponse(false, ErrorType.BusinessObjectNotFound.value))
      }
    case req @ PUT -> Root / "quests" =>
      req.decode[Questionnaire] { quest =>
        update(quest).flatMap(_ => Accepted(HttpResponse(true, ())))
      }.handleErrorWith(_ => BadRequest(HttpResponse(false, ErrorType.UpdateFailed.value)))
  }
}

object QuestionnaireService {
  def apply[F[_]: Sync](
    questionnaireRepositoryInterpreter: QuestionnaireRepositoryInterpreter[F]
  ): QuestionnaireService[F] = new QuestionnaireService[F](questionnaireRepositoryInterpreter)
}