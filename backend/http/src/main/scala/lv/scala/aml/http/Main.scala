package lv.scala.aml.http

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import doobie.quill.DoobieContext
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.config.{Config, ServerConfig}
import lv.scala.aml.database.repository.interpreter.AccountRepositoryInterpreter
import lv.scala.aml.database.{Database, DbInit}
import lv.scala.aml.http.routes.AccountRoutes
import lv.scala.aml.http.services.AccountService
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp{

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private lazy val ctx: MySQL[CamelCase] with Decoders with Encoders =
    new MySQL[CamelCase](CamelCase) with Decoders with Encoders

 // private val routes: HttpRoutes[IO] = new AccountRoutes[IO].routes

  // ToDo: Concider using middleware to TimeOut request & global error handling
  def makeRouter(transactor: HikariTransactor[IO]): Kleisli[IO, Request[IO], Response[IO]] =
    {
      val accountInterpreter: AccountRepositoryInterpreter[IO] = new AccountRepositoryInterpreter[IO](transactor, ctx)
      val accountService: AccountService[IO] = new AccountService[IO](accountInterpreter)
      (new AccountRoutes[IO](accountService).routes).orNotFound
    }
  // routes.orNotFound

  def stream(serverConfig: ServerConfig, transactor: HikariTransactor[IO]): fs2.Stream[IO, ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(makeRouter(transactor))
      .serve

  // ToDo: auto populate DB, run  API
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
