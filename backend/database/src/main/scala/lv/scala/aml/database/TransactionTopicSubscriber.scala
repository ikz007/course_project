package lv.scala.aml.database

import cats.Applicative
import lv.scala.aml.common.dto.responses.KafkaErrorMessage
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.kafka.Serdes._
import lv.scala.aml.kafka.{KafkaConsumer, KafkaErrorProducer, KafkaMessage}
import cats.effect.{ConcurrentEffect, ContextShift, IO, Resource, Sync, Timer}
import doobie.hikari.HikariTransactor
import lv.scala.aml.common.dto.Transaction
import fs2.kafka.CommittableOffsetBatch
import fs2.Chunk
import doobie.implicits._
import cats.syntax.all._
import doobie.Update
import doobie.util.meta.Meta
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.scenario.ScenarioConfiguration

import java.util.Date
import java.time.Instant

class TransactionTopicSubscriber[F[_]: Sync](
  xa: HikariTransactor[F],
  kafkaConsumer: KafkaConsumer[F, Transaction],
  kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage],
  scenarioConfiguration: ScenarioConfiguration
) {

  private val logger = Slf4jLogger.getLogger[F]
  implicit val InstantMeta: Meta[Instant] = Meta[Date].timap(_.toInstant)(Date.from)
  def subscribe = {
    logger.info("Subscribed to the Kafka Consumer...")
    val trnsStream = kafkaConsumer.stream.evalMap { chunk =>
      createTransactions(chunk.map { case (update, _) => update })
        .map(_ -> CommittableOffsetBatch.fromFoldable(chunk.map {
          case (_, offset) => offset
        }))
    }
    trnsStream.evalMap{
      case (x,offsetBatch) => logger.info(s"$x messages commited") *> offsetBatch.commit.as(x)
    }
  }

  private def checkForAlert(
    transactions: Chunk[Transaction]
  ): F[Int] = {
    transactions.traverse(transaction => {
      val matchedKeywords = scenarioConfiguration.keywords.filter(transaction.Description.contains(_))
      if (!matchedKeywords.isEmpty) {
        insertAlert("MatchedKeywords", matchedKeywords.mkString(","), transaction.Reference, transaction.OurIBAN)
      } else if (scenarioConfiguration.highRiskCountries.contains(transaction.CountryCode)) {
        insertAlert("HighRiskCountry", transaction.CountryCode, transaction.Reference, transaction.OurIBAN)
      } else if (transaction.Amount > scenarioConfiguration.defaultMaxThreshold) {
        insertAlert("ThresholdCheck", transaction.Amount.toString(), transaction.Reference, transaction.OurIBAN)
      } else {
        Sync[F].pure(0)
      }
    }
    ).map(_.sumAll)
  }

  private def insertAlert(
    alertedCondition: String,
    alertedValue: String,
    transactionReference: String,
    subject: String,
    scenarioName: String = "RealTime",
    subjectType: String = "Account"
  ): F[Int] = {
    sql"""insert ignore into Alert
        (Subject, SubjectType, TransactionReferences,
        AlertedCondition, AlertedValue, DateCreated,
        ScenarioName)
        values ($subject, $subjectType, $transactionReference, $alertedCondition, $alertedValue, NOW(), $scenarioName);
        """.update.withUniqueGeneratedKeys[Int]("AlertId").transact(xa)
      .handleErrorWith(err =>
      logger.info(s"Failed to create alert in DB, ${err.getMessage}")  *> Sync[F].pure(0)
    )


  }

  private def createTransactions(
    messages: Chunk[KafkaMessage[Transaction]]
  ): F[Int] = {
    val sqlInsert =
      """
           insert ignore into Transaction
        |(OurIBAN, TheirIBAN, Reference, TransactionCode, BookingDateTime,
        |DebitCredit, Amount, Currency, Description, CountryCode)
        |values (?,?,?,?,?,?,?,?,?,?);
         """.stripMargin
    val transactions = messages.map(_.message)

    for {
      _ <- checkForAlert(transactions)
      res <- Update[Transaction](sqlInsert)
        .updateMany(transactions)
        .transact(xa)
        .handleErrorWith { err =>
          logger.error(s"Failed to insert transaction in DB, ${err.getMessage}") *> Sync[F].pure(0)
        }
    } yield res




    //    logger.info("Inserting transactions in db...")
    //    val trns = transactions
    //      .toVector
    //      .groupBy(_.requestId)
    //      .view
    //      .mapValues(_.maxBy(_.requestId))
    //      .values
    //      .toVector
    //      .map{
    //        case KafkaMessage(_, transaction) => ( transaction)
    //      }
    //
    //
    //      run {
    //          liftQuery(trns).foreach { t =>
    //            query[Transaction].insert(t)
    //          }
    //        }.transact(xa).map{s => s.length}.handleErrorWith{err =>
    //        logger.info(s"Failed to insert transaction in DB, ${err.getMessage}")  *> Sync[F].pure(0)
    //      }
    //*> kafkaErrorProducer.produceOne(KafkaErrorMessage(err.getMessage, ""))
  }
}

object TransactionTopicSubscriber {
  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
    xa: HikariTransactor[F],
    kafkaConfig: KafkaConfig,
    scenarioConfiguration: ScenarioConfiguration
  ): Resource[F, TransactionTopicSubscriber[F]] = for {
    trnsErrorProducer <- KafkaErrorProducer.apply[F, KafkaErrorMessage](kafkaConfig)
    transactionConsumer <- KafkaConsumer.apply[F, Transaction](kafkaConfig, trnsErrorProducer)
  } yield new TransactionTopicSubscriber[F](xa, transactionConsumer, trnsErrorProducer, scenarioConfiguration)


}