package lv.scala.aml.kafka

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import cats.implicits._
import fs2.{Chunk, Stream}
import fs2.kafka.{ConsumerSettings, _}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.responses.KafkaErrorMessage
import lv.scala.aml.config.KafkaConfig

import scala.concurrent.duration._
trait KafkaConsumer[F[_],A] {
  def stream: Stream[F, Chunk[(KafkaMessage[A], CommittableOffset[F])]]
}

object KafkaConsumer {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative, A](
    kafkaConfig: KafkaConfig,
    kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage]
  )(implicit d: Deserializer[F, Either[Throwable, KafkaMessage[A]]]
  ): Resource[F, KafkaConsumerImpl[F, A]] =
    fs2.kafka.KafkaConsumer.resource(consumerSettings[F, Unit, Serdes.Attempt[KafkaMessage[A]]](kafkaConfig))
      .evalMap{ c =>
      c.subscribeTo(kafkaConfig.consumerTopic).as {
                new KafkaConsumerImpl(c, kafkaErrorProducer = kafkaErrorProducer)
      }
    }

  def consumerSettings[F[_] : Sync, K, V](
    kafkaConfig: KafkaConfig
  )(implicit kd: Deserializer[F, K], vd: Deserializer[F, V]): ConsumerSettings[F, K, V] =
    ConsumerSettings[F, K, V]
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(kafkaConfig.groupId)
      // manual commits, in order not to miss anything in case of unexpected error
      .withEnableAutoCommit(false)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
}

class KafkaConsumerImpl[F[_]: ConcurrentEffect : ContextShift : Timer, A](
  consumer: fs2.kafka.KafkaConsumer[F, Unit, Serdes.Attempt[KafkaMessage[A]]],
  maxPerBatch: Int = 5,
  batchTime: FiniteDuration = 5.seconds,
  kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage]
) extends KafkaConsumer[F, A] {
  private val logger = Slf4jLogger.getLogger[F]


  // publish errors to error stream
  def stream: Stream[F, Chunk[(KafkaMessage[A], CommittableOffset[F])]] =
    consumer.stream.evalTap { msg =>
      //set to debug
      logger.info(s"Processing message from the topic ${msg.record.topic}")
    }.map{ msg =>
      msg.record.value.map(_ -> msg.offset)
    }.evalTap {
            //todo:
          // how to publish failed message to the Kafka Error topic?
      case Left(err) => logger.error(err)(s"Failed to import the business object...")
      case Right((msg, _)) => logger.info(s"Business object was successfully parsed $msg.")
    }.rethrow.groupWithin(maxPerBatch, batchTime)
      .handleErrorWith{ _ =>
        Stream.eval(logger.info("Error has occured while processing kafka consumer")) >> stream
      }
}