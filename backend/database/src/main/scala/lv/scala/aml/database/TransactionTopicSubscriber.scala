package lv.scala.aml.database

import cats.Applicative
import lv.scala.aml.common.dto.responses.KafkaErrorMessage
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes._
import lv.scala.aml.kafka.{KafkaConsumer, KafkaErrorProducer, KafkaMessage}
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Transaction
import fs2.kafka.CommittableOffsetBatch
import fs2.Chunk
import doobie.implicits._
import cats.syntax.all._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class TransactionTopicSubscriber[F[_]: Sync](
  xa: HikariTransactor[F],
  kafkaConsumer: KafkaConsumer[F, Transaction],
  kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends Schema {
    import ctx._
  private val logger = Slf4jLogger.getLogger[F]

  def subscribe = {
    logger.info("Subscribed to the Kafka Consumer...")
    val trnsStream = kafkaConsumer.stream.evalMap { chunk =>
      createTransactions(chunk.map { case (update, _) => update })
        .map(_ -> CommittableOffsetBatch.fromFoldable(chunk.map {
          case (_, offset) => offset
        }))
    }
    trnsStream.evalMap{
      case (x,offsetBatch) => offsetBatch.commit.as(x)
    }
  }

  private def createTransactions(
    transactions: Chunk[KafkaMessage[Transaction]]
  ): F[Int] = {
    logger.info("Inserting transactions in db...")
    val trns = transactions
      .toVector
      .groupBy(_.requestId)
      .view
      .mapValues(_.maxBy(_.requestId))
      .values
      .toVector
      .map{
        case KafkaMessage(_, transaction) => ( transaction)
      }

      run {
          liftQuery(trns).foreach { t =>
            query[Transaction].insert(t)
          }
        }.transact(xa).map{s => s.length}.handleErrorWith{err =>
        logger.info(s"Failed to insert transaction in DB, ${err.getMessage}")  *> Sync[F].pure(0)
        // why return 0 ?
      }
//*> kafkaErrorProducer.produceOne(KafkaErrorMessage(err.getMessage, ""))
  }
}

object TransactionTopicSubscriber {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
    xa: HikariTransactor[F],
    kafkaConfig: KafkaConfig,
  ): Resource[F, TransactionTopicSubscriber[F]] = for {
    trnsErrorProducer <- KafkaErrorProducer.apply[F, KafkaErrorMessage](kafkaConfig)
    transactionConsumer <- KafkaConsumer.apply[F, Transaction](kafkaConfig, trnsErrorProducer)
  } yield new TransactionTopicSubscriber[F](xa, transactionConsumer, trnsErrorProducer,new MySQL[CamelCase](CamelCase) with Decoders with Encoders)


}