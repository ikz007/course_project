package lv.scala.aml.kafka

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits.catsSyntaxApply
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, commitBatchWithin}
import io.chrisdavenport.log4cats.Logger
import lv.scala.aml.common.dto.{InvalidMessage, Transaction}
import lv.scala.aml.config.KafkaConfig

import scala.concurrent.duration.DurationInt

object KafkaReceiver {
  def create[F[_]: Logger: Sync: ContextShift: ConcurrentEffect: Timer:Applicative]
  (kafkaConfig:KafkaConfig)
  (decodeTransactionJson: String => Either[InvalidMessage, Transaction],
   sendError:  InvalidMessage => F[Unit],
   saveTransaction: Transaction => F[Unit],
   checkTransaction: Transaction => F[Unit],
  ) = {
    val consumerSettings = ConsumerSettings[F, Unit, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(kafkaConfig.groupId)
      .withClientId(kafkaConfig.clientId)

    fs2.kafka.KafkaConsumer.stream(consumerSettings)
      .evalTap(_.subscribeTo(kafkaConfig.consumerTopic))
      .flatMap(_.stream)
      .evalMap( consumerRecord => {
        decodeTransactionJson(consumerRecord.record.value)
          .fold(
            sendError,
            tr => saveTransaction(tr) *>  checkTransaction(tr) *> Sync[F].unit) *> Sync[F].delay(consumerRecord.offset)
      })
      .through(commitBatchWithin(500, 5.seconds))
  }
}