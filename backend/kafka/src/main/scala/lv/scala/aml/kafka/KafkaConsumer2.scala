package lv.scala.aml.kafka

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits.catsSyntaxApply
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, commitBatchWithin}
import io.chrisdavenport.log4cats.Logger
import lv.scala.aml.common.dto.Transaction

import scala.concurrent.duration.DurationInt

case class InvalidTranscation(transcationRecord:String, err:String)

object KafkaReceiver {
  def create[F[_]: Logger: Sync: ContextShift: ConcurrentEffect: Timer:Applicative]
  //(kafkaConfig:KafkaConfig)
                                                                       (decodeTransactionJson: String => Either[InvalidTranscation, Transaction],
                                                                        sendError:  InvalidTranscation => F[Unit],
                                                                        saveTransaction: Transaction => F[Unit],
                                                                        checkTransaction:Transaction => F[Unit], // if rules succeeds sends to another kafka or sets flag in db
                                                                       ): F[Unit] = {
    val consumerSettings = ConsumerSettings[F, Unit, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      //.withBootstrapServers(bootStrapServers)
      //.withGroupId(groupId)
      //.withClientId(clientId)
    val topic :String = ???

    fs2.kafka.KafkaConsumer.stream(consumerSettings)
      .evalTap(_.subscribeTo(topic))
      .flatMap(_.stream)
      //.groupWithin(1000, 1.minute) //if you want to micro batch
      .evalMap( consumerRecord => {
        decodeTransactionJson(consumerRecord.record.value)
          .fold( sendError, tr => saveTransaction(tr) *>  checkTransaction(tr)) *> Sync[F].delay(consumerRecord.offset)
      })
      .through(commitBatchWithin(500, 5.seconds))
      .compile
      .drain
  }
}
