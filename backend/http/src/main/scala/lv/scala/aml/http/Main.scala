package lv.scala.aml.http

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.config.Config
import lv.scala.aml.database.repository.interpreter._
import lv.scala.aml.database.utils.AmlRuleChecker
import lv.scala.aml.database.{Database, ScenarioConfigRetriever, TransactionTopicSubscriber}
import lv.scala.aml.http.services._
import lv.scala.aml.kafka.KafkaErrProduce
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.http4s.{HttpRoutes, Request, Response}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp{

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private lazy val ctx: MySQL[CamelCase] with Decoders with Encoders =
    new MySQL[CamelCase](CamelCase) with Decoders with Encoders

  // ToDo: Concider using middleware to TimeOut request & global error handling
  def makeRouter(transactor: HikariTransactor[IO]): Kleisli[IO, Request[IO], Response[IO]] =
    {
      val accountInterpreter: AccountRepositoryInterpreter[IO] = new AccountRepositoryInterpreter[IO](transactor, ctx)
      val countryInterpreter: CountryRepositoryInterpreter[IO] = new CountryRepositoryInterpreter[IO](transactor, ctx)
      val questionnaireIntepreter: QuestionnaireRepositoryInterpreter[IO] = new QuestionnaireRepositoryInterpreter[IO](transactor, ctx)
      val relationshipInterpreter: RelationshipRepositoryInterpreter[IO] = new RelationshipRepositoryInterpreter[IO](transactor, ctx)
      val transactionInterpreter: TransactionRepositoryInterpreter[IO] = new TransactionRepositoryInterpreter[IO](transactor, ctx)
      val customerInterpreter: CustomerRepositoryInterpreter[IO] = new CustomerRepositoryInterpreter[IO](transactor, ctx)
      val alertInterpreter: AlertRepositoryInterpreter[IO] = new AlertRepositoryInterpreter[IO](transactor, ctx)

      val accountRoutes = AccountService[IO](accountInterpreter).routes
      val countryRoutes = CountryService[IO](countryInterpreter).routes
      val questionaireRoutes = QuestionnaireService[IO](questionnaireIntepreter).routes
      val relationshipRoutes = RelationshipService[IO](relationshipInterpreter).routes
      val transactionRoutes = TransactionService[IO](transactionInterpreter).routes
      val customerRoutes = CustomerService[IO](customerInterpreter).routes
      val alertRoutes = AlertService[IO](alertInterpreter).routes

      withCors(accountRoutes <+> countryRoutes <+> questionaireRoutes <+> relationshipRoutes <+> transactionRoutes <+> customerRoutes <+> alertRoutes).orNotFound
    }

  def stream(config: Config, transactor: HikariTransactor[IO]) =
    BlazeServerBuilder[IO](global)
      .bindHttp(config.server.port, config.server.host)
      .withHttpApp(makeRouter(transactor))
      .withWebSockets(true)
      .serve


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
      config <- Config.load[IO]()
      xa <- IO.pure(Database.buildTransactor[IO](Database.TransactorConfig(config.db)))
     // _ <- DbInit.initialize[IO](xa)
      scenarioSettings <- ScenarioConfigRetriever(xa).retrieveConfiguration
      amlRuleChecker <- IO.delay(AmlRuleChecker[IO](xa, scenarioSettings))
      kafkaErrProducer <- IO.delay(KafkaErrProduce[IO](config.kafka))
      _ <- new TransactionTopicSubscriber[IO](xa, config.kafka, kafkaErrProducer, amlRuleChecker).subscribe2.start
      exitCode <- stream(config,xa).compile.drain.map(_ => ExitCode.Success) // .use(_.lastOrError) //.compile.drain.map(_ => ExitCode.Success)
    } yield exitCode
}
