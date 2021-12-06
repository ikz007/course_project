package lv.scala.aml.database

import cats.Applicative
import lv.scala.aml.common.dto.responses.KafkaErrorMessage
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes._
import lv.scala.aml.kafka.{KafkaConsumer, KafkaErrorProducer}
import doobie.implicits._
import cats.syntax.all._
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Relationship, Transaction}
import fs2.kafka.CommittableOffsetBatch
import fs2.{Chunk, Stream}
import lv.scala.aml.database.Schema
import io.chrisdavenport.log4cats.Logger
import doobie.implicits._
import cats.syntax.all._
import io.circe.Json

class TransactionTopicSubscriber[F[_]: Sync](
  xa: HikariTransactor[F],
  kafkaConsumer: KafkaConsumer[F, (String, Json)],
  kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends Schema {
    import ctx._

  def subscribe: Stream[F, Unit] = {
    val trnsStream = kafkaConsumer.stream.evalMap{ chunk =>
      
    }
  }
}

object TransactionTopicSubscriber {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
    xa: HikariTransactor[F],
    kafkaConfig: KafkaConfig,
  ): Resource[F, TransactionTopicSubscriber[F]] = for {
    transactionConsumer <- KafkaConsumer.apply[F, (String, Json)](kafkaConfig)
    trnsErrorProducer <- KafkaErrorProducer.apply[F, KafkaErrorMessage](kafkaConfig)
  } yield new TransactionTopicSubscriber[F](xa, transactionConsumer, trnsErrorProducer,new MySQL[CamelCase](CamelCase) with Decoders with Encoders)


}