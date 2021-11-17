package lv.scala.aml.http

import cats.effect.{ExitCode, IO, IOApp}
import lv.scala.aml.config.{Config, ServerConfig}
import lv.scala.aml.database.Database
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp{


  def stream(serverConfig: ServerConfig) =
    BlazeServerBuilder[IO](global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .serve

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config.load()
      xa <- IO.pure(Database.buildTransactor(Database.TransactorConfig(config.dbConfig)))
      _ <- Database.bootstrap(xa)
      exitCode <- stream(config.serverConfig).compile.drain.map(_ => ExitCode.Success)
    } yield exitCode
}
