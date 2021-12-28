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
import lv.scala.aml.database.utils.AmlRuleChecker
import lv.scala.aml.database.{Database, DbInit, TransactionTopicSubscriber}
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

  private[http] def makeRouter(transactor: HikariTransactor[IO]): Kleisli[IO, Request[IO], Response[IO]] =
    {
      val accountRoutes = AccountService[IO](transactor, ctx).routes
      val countryRoutes = CountryService[IO](transactor, ctx).routes
      val questionnaireRoutes = QuestionnaireService[IO](transactor, ctx).routes
      val relationshipRoutes = RelationshipService[IO](transactor, ctx).routes
      val transactionRoutes = TransactionService[IO](transactor, ctx).routes
      val customerRoutes = CustomerService[IO](transactor, ctx).routes
      val alertRoutes = AlertService[IO](transactor, ctx).routes

      val routes = Seq(
        accountRoutes,
        countryRoutes,
        questionnaireRoutes,
        relationshipRoutes,
        transactionRoutes,
        customerRoutes,
        alertRoutes
      ).reduce(_ <+> _)

      withCors(routes).orNotFound
    }

  private def stream(config: Config, transactor: HikariTransactor[IO]) =
    BlazeServerBuilder[IO](global)
      .bindHttp(config.server.port, config.server.host)
      .withHttpApp(makeRouter(transactor))
      .withWebSockets(true)
      .serve


  private def withCors(svc: HttpRoutes[IO]): HttpRoutes[IO] =
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

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- logger.info("Server starting...")
      config <- Config.load[IO]()
      xa <- IO.delay(Database.buildTransactor[IO](Database.TransactorConfig(config.db)))
      _ <- DbInit.initialize[IO](xa)
      amlRuleChecker <- AmlRuleChecker[IO](xa)
      kafkaErrProducer <- IO.delay(KafkaErrProduce[IO](config.kafka))
      _ <- new TransactionTopicSubscriber[IO](xa, config.kafka, kafkaErrProducer, amlRuleChecker).subscribe2.start
      exitCode <- stream(config,xa).compile.drain.map(_ => ExitCode.Success)
    } yield exitCode
}
