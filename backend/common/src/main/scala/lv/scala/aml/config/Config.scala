package lv.scala.aml.config

import cats.effect.Sync
import cats.syntax.all._
import io.circe.config.parser
import io.circe.generic.auto._

case class ServerConfig(host: String, port: Int)

case class DBConfig(url: String, username: String, password: String, driver: String, poolSize: Int)

case class Config(server: ServerConfig, db: DBConfig, kafka: KafkaConfig)

case class KafkaConfig(bootstrapServers: String, consumerTopic: String, producerTopic: String, clientId: String, groupId: String)

object Config {
  def load[F[_]: Sync](): F[Config] =
    for {
      conf <- parser.decodePathF[F, Config]("aml_setup")
    } yield conf
}
