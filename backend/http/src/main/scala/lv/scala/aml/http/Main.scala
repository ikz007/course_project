package lv.scala.aml.http

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.config.{Config, ServerConfig}
import lv.scala.aml.database.{Database, DbInit}
import lv.scala.aml.http.routes.AccountRoutes
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp{

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val routes: HttpRoutes[IO] = new AccountRoutes[IO].routes

  def makeRouter(transactor: HikariTransactor[IO]): Kleisli[IO, Request[IO], Response[IO]] =
   routes.orNotFound

  def stream(serverConfig: ServerConfig, transactor: HikariTransactor[IO]): fs2.Stream[IO, ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(makeRouter(transactor))
      .serve

  // ToDo: autopopulate DB, run  API
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- logger.info("Server starting...")
      config <- Config.load()
      xa <- IO.pure(Database.buildTransactor(Database.TransactorConfig(config.dbConfig)))
     // _ <- Database.bootstrap(xa)
      _ <- DbInit.initialize(xa)
      exitCode <- stream(config.serverConfig, xa).compile.drain.map(_ => ExitCode.Success)
    } yield exitCode
}
