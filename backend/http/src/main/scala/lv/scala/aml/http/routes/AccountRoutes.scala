package lv.scala.aml.http.routes

import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import lv.scala.aml.http.services.AccountService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import lv.scala.aml.common.dto.Account
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
// Should Akka be used for routing instead?
// ToDo: Replace IO in all project with F to simplify testing
class AccountRoutes[F[_]: Sync: Logger](
  accountService: AccountService[F]
) extends Http4sDsl[F] {

  // authentication?
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "accounts" / "all" =>
      accountService.get.flatMap { case list => Ok(list.asJson)
      case Nil => NotFound()
      }
    case _ @ GET -> Root / "accounts" / id =>
      accountService.getById(id).value.flatMap {
        case Some(account) => Ok(account.asJson)
        case None => NotFound("Such account does not exist")
      }
    case req @ PUT -> Root / "accounts" =>
      req.decode[Account] { acc =>
        accountService.update(acc).flatMap(_ => Accepted())
      }.handleErrorWith(e => BadRequest(e.getMessage))
  }

  private val prefixPath = "/api/v1"

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
