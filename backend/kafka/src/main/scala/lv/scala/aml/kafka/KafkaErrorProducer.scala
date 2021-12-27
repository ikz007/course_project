package lv.scala.aml.kafka

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import fs2.kafka._
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import lv.scala.aml.common.dto.InvalidMessage
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes.encodingSer

class KafkaErrProduce[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
  kafkaConfig: KafkaConfig,
  kafkaStream: fs2.Stream[F, KafkaProducer[F, Unit, InvalidMessage]]
) {

  def streamProduce(message: InvalidMessage) =
   kafkaStream.evalMap{ producer =>
      val rs = ProducerRecords.one(ProducerRecord(kafkaConfig.producerTopic, (), message))
      producer.produce(rs).flatten }.compile.drain
}

object KafkaErrProduce {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
    kafkaConfig: KafkaConfig
  ) = new KafkaErrProduce(kafkaConfig, KafkaProducer[F].stream(producerSettings(kafkaConfig)))

  private def producerSettings[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative] (
    kafkaConfig: KafkaConfig
  ): ProducerSettings[F, Unit, InvalidMessage] =
    ProducerSettings[F, Unit, InvalidMessage]
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withClientId(kafkaConfig.clientId)
      .withRetries(Int.MaxValue)
      .withEnableIdempotence(true)
}