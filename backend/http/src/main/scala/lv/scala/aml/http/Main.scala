package lv.scala.aml.http

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import cats.syntax.all._
import lv.scala.aml.config.{Config, KafkaConfig, ServerConfig}
import lv.scala.aml.database.repository.interpreter.{AccountRepositoryInterpreter, CountryRepositoryInterpreter, CustomerRepositoryInterpreter, QuestionnaireRepositoryInterpreter, RelationshipRepositoryInterpreter, TransactionRepositoryInterpreter}
import lv.scala.aml.database.{Database, DbInit, TransactionTopicSubscriber}
import lv.scala.aml.http.services.{AccountService, CountryService, CustomerService, QuestionnaireService, RelationshipService, TransactionService}
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}

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
      val countryInterpreter: CountryRepositoryInterpreter[IO] = new CountryRepositoryInterpreter[IO](transactor, ctx)
      val questionnaireIntepreter: QuestionnaireRepositoryInterpreter[IO] = new QuestionnaireRepositoryInterpreter[IO](transactor, ctx)
      val relationshipInterpreter: RelationshipRepositoryInterpreter[IO] = new RelationshipRepositoryInterpreter[IO](transactor, ctx)
      val transactionInterpreter: TransactionRepositoryInterpreter[IO] = new TransactionRepositoryInterpreter[IO](transactor, ctx)
      val customerInterpreter: CustomerRepositoryInterpreter[IO] = new CustomerRepositoryInterpreter[IO](transactor, ctx)

      val accountRoutes = AccountService[IO](accountInterpreter).routes
      val countryRoutes = CountryService[IO](countryInterpreter).routes
      val questionaireRoutes = QuestionnaireService[IO](questionnaireIntepreter).routes
      val relationshipRoutes = RelationshipService[IO](relationshipInterpreter).routes
      val transactionRoutes = TransactionService[IO](transactionInterpreter).routes
      val customerRoutes = CustomerService[IO](customerInterpreter).routes

      withCors(accountRoutes <+> countryRoutes <+> questionaireRoutes <+> relationshipRoutes <+> transactionRoutes <+> customerRoutes).orNotFound
    }
  // routes.orNotFound

  def stream(serverConfig: ServerConfig, kafka: KafkaConfig, transactor: HikariTransactor[IO]) =
    for {
      listener <- TransactionTopicSubscriber[IO](transactor, kafka)
    } yield BlazeServerBuilder[IO](global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(makeRouter(transactor))
      .serve
      .concurrently(listener.subscribe)
      .compile

  def withCors(svc: HttpRoutes[IO]): HttpRoutes[IO] =
    CORS(
      svc,
      CORSConfig(
        anyOrigin = true,
        anyMethod = false,
        allowedMethods = Some(Set("GET", "POST", "PUT", "HEAD", "OPTIONS")),
        allowedHeaders = Some(Set("*")),
        allowCredentials = true,
        maxAge = 1800
      )
    )

  // ToDo: auto populate DB, run  API
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- logger.info("Server starting...")
      config <- Config.load()
      xa <- IO.pure(Database.buildTransactor(Database.TransactorConfig(config.dbConfig)))
     // _ <- Database.bootstrap(xa)
      _ <- DbInit.initialize[IO](xa)
      exitCode <- stream(config.serverConfig, config.kafkaConfig, xa).use(_.lastOrError) //.compile.drain.map(_ => ExitCode.Success)
    } yield exitCode
}
