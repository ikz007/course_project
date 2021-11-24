package lv.scala.aml.http.routes

import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

// Should Akka be used for routing instead?
// ToDo: Replace IO in all project with F to simplify testing
class AccountRoutes[F[_]: Sync: Logger] extends Http4sDsl[F] {

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "accounts" => Ok("account") // call repo to get accounts, handle error (?)
    case _ @ GET -> Root / "accounts" / id => Ok(id) // call repo to get account
    case req @ PUT -> Root / "accounts" / id => Ok("updated") // call repo to update account, handle errors
  }

  private val prefixPath = "/api/v1"

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
