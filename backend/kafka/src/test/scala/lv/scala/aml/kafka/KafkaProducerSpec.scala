package lv.scala.aml.kafka

import cats.effect.IO
import cats.implicits._
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings}
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes.encodingSer
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import lv.scala.aml.common.dto.NotValidatedTransaction

class KafkaProducerSpec(
  kafkaConfig: KafkaConfig,
  kafkaStream: fs2.Stream[IO, KafkaProducer[IO, Unit, NotValidatedTransaction]]
) {
  // initialize one time
  def streamProduce(message: NotValidatedTransaction, topic: String = kafkaConfig.consumerTopic) =
    kafkaStream.evalMap{ producer =>
      val rs = ProducerRecords.one(ProducerRecord(topic, (), message))
      producer.produce(rs).flatten }.compile.drain
}

object KafkaProducerSpec {
  def apply(
    kafkaConfig: KafkaConfig
  ) = new KafkaProducerSpec(kafkaConfig, KafkaProducer[IO].stream(producerSettings(kafkaConfig)))

  private def producerSettings (
    kafkaConfig: KafkaConfig
  ): ProducerSettings[IO, Unit, NotValidatedTransaction] =
    ProducerSettings[IO, Unit, NotValidatedTransaction]
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withClientId(kafkaConfig.clientId)
      .withRetries(Int.MaxValue)
      .withEnableIdempotence(true)
}