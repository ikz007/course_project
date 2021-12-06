package lv.scala.aml.config

import cats.effect.IO
import io.circe.config.parser
import io.circe.generic.auto._

case class ServerConfig(host: String, port: Int)

case class DBConfig(url: String, username: String, password: String, poolSize: Int)

case class Config(serverConfig: ServerConfig, dbConfig: DBConfig, kafkaConfig: KafkaConfig)

case class KafkaConfig(bootstrapServers: String, consumerTopic: String, producerTopic: String, clientId: String, groupId: String)

object Config {
  def load(): IO[Config] =
    for {
      dbConf <- parser.decodePathF[IO, DBConfig]("db")
      serverConf <- parser.decodePathF[IO, ServerConfig]("server")
      kafkaConf <- parser.decodePathF[IO, KafkaConfig]("kafka")
    } yield Config(serverConf, dbConf, kafkaConf)
}
