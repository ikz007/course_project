package lv.scala.aml.kafka

import cats.Applicative
import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import fs2.kafka.{ProducerRecord, ProducerRecords, ProducerSettings, Serializer, _}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import lv.scala.aml.common.dto.InvalidMessage
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes.encodingSer

//trait KafkaErrorProducer[F[_]] {
//  def produce(messages: List[InvalidMessage]): F[Unit]
//  def produceOne(message: InvalidMessage): F[Unit]
//}
//
//object KafkaErrorProducer {
//  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
//    kafkaConfig: KafkaConfig
//  )(implicit d: Serializer[F, InvalidMessage]
//  ): Resource[F, KafkaErrorProducerImpl[F]] =
//    fs2.kafka.KafkaProducer.resource(producerSettings[F, Unit, InvalidMessage](kafkaConfig))
//      .map{ prdc => new KafkaErrorProducerImpl(prdc,kafkaConfig)}
//
//  def producerSettings[F[_] : Sync, K, V](
//    kafkaConfig: KafkaConfig
//  )(implicit kd: Serializer[F, K], vd: Serializer[F, V]): ProducerSettings[F, K, V] =
//    ProducerSettings[F, K, V]
//      .withBootstrapServers(kafkaConfig.bootstrapServers)
//      .withClientId(kafkaConfig.clientId)
//      .withRetries(Int.MaxValue)
//      .withEnableIdempotence(true)
//}
//
//
//class KafkaErrorProducerImpl[F[_]: ConcurrentEffect : ContextShift : Timer](
//  producer: KafkaProducer[F, Unit, InvalidMessage],
//  kafkaConfig: KafkaConfig
//) extends KafkaErrorProducer[F] {
//  private val logger = Slf4jLogger.getLogger[F]
//  override def produce(messages: List[InvalidMessage]): F[Unit] = {
//    val errors = messages.map{ case msg => ProducerRecord(kafkaConfig.producerTopic, (), msg)}
//    for {
//      _ <- logger.info(s"Submitting ${messages.length} error messages to ${kafkaConfig.producerTopic}")
//      prdc <- producer.produce(ProducerRecords(errors))
//      _ <- prdc // get rid of IO
//    } yield ()
//  }
//  def producerSettings[F[_] : Sync, K, V](
//    kafkaConfig: KafkaConfig
//  )(implicit kd: Serializer[F, K], vd: Serializer[F, V]): ProducerSettings[F, K, V] =
//    ProducerSettings[F, K, V]
//      .withBootstrapServers(kafkaConfig.bootstrapServers)
//      .withClientId(kafkaConfig.clientId)
//      .withRetries(Int.MaxValue)
//      .withEnableIdempotence(true)
//
//  override def produceOne(message: InvalidMessage): F[Unit] = for {
//    _ <- logger.info(s"Submitting error message to ${kafkaConfig.producerTopic}")
//    prdc <- producer.produceOne_(ProducerRecord(kafkaConfig.producerTopic, (), message))
//    _ <- prdc
//  } yield ()
//
//}


class KafkaErrProduce[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
  kafkaConfig: KafkaConfig,
  kafkaStream: fs2.Stream[F, KafkaProducer[F, Unit, InvalidMessage]]
) {
  // initialize one time
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