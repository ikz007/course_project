package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.{Concurrent, Sync}
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.Alert
import lv.scala.aml.database.repository.interpreter.AlertRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

class AlertService[F[_]: Sync : Concurrent](
  alertRepo: AlertRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Alert]] = alertRepo.get

  private def getById(id: Int): OptionT[F, Alert] = alertRepo.getById(id)

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case _@GET -> Root / "alerts" / "all" =>
        get.flatMap { case list => Ok(list.asJson)
        case Nil => NotFound()
        }
      case GET -> Root / "alerts" / "subscribe" =>
        WebSocketBuilder[F].build(
          send = alertRepo.getStream.map(_.asJson.toString()).map(WebSocketFrame.Text(_)),
          receive = stream => stream.drain
        )
    }

}

object AlertService {
  def apply[F[_]: Sync : Concurrent](
    alertRepo: AlertRepositoryInterpreter[F]
  ): AlertService[F] = new AlertService[F](alertRepo)
}