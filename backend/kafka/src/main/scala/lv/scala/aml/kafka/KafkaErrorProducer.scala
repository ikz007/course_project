package lv.scala.aml.kafka

import cats.Applicative
import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import fs2.kafka.{ProducerRecord, ProducerRecords, ProducerSettings, Serializer, _}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes.encodingSer

trait KafkaErrorProducer[F[_],A] {
  def produce(messages: List[KafkaMessage[A]]): F[Unit]
}

object KafkaErrorProducer {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative, A](
    kafkaConfig: KafkaConfig
  )(implicit d: Serializer[F, KafkaMessage[A]]
  ): Resource[F, KafkaErrorProducerImpl[F, A]] =
    fs2.kafka.KafkaProducer.resource(producerSettings[F, Unit, KafkaMessage[A]](kafkaConfig))
      .map{ prdc => new KafkaErrorProducerImpl(prdc,kafkaConfig)}

  def producerSettings[F[_] : Sync, K, V](
    kafkaConfig: KafkaConfig
  )(implicit kd: Serializer[F, K], vd: Serializer[F, V]): ProducerSettings[F, K, V] =
    ProducerSettings[F, K, V]
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withClientId(kafkaConfig.clientId)
      .withRetries(Int.MaxValue)
      .withEnableIdempotence(true)
}


class KafkaErrorProducerImpl[F[_]: ConcurrentEffect : ContextShift : Timer, A](
  producer: KafkaProducer[F, Unit, KafkaMessage[A]],
  kafkaConfig: KafkaConfig
) extends KafkaErrorProducer[F, A] {
  private val logger = Slf4jLogger.getLogger[F]
  override def produce(messages: List[KafkaMessage[A]]): F[Unit] = {
    val errors = messages.map{ case msg => ProducerRecord(kafkaConfig.producerTopic, (), msg)}
    for {
      _ <- logger.info(s"Submitting ${messages.length} error messages to ${kafkaConfig.producerTopic}")
      prdc <- producer.produce(ProducerRecords(errors))
      _ <- prdc // get rid of IO
    } yield ()
  }
}
