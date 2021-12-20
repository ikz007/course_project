package lv.scala.aml.database

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.syntax.all._
import doobie.Update
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.meta.Meta
import fs2.Chunk
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.Transaction
import lv.scala.aml.common.dto.parser.TransactionParser
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.database.utils.AmlRuleChecker
import lv.scala.aml.kafka.{KafkaErrProduce, KafkaMessage, KafkaReceiver}

import java.time.LocalDate

class TransactionTopicSubscriber[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Timer: Applicative](
  xa: HikariTransactor[F],
  kafkaConfig: KafkaConfig,
  amlRuleChecker: AmlRuleChecker[F]
 // kafkaConsumer: KafkaConsumer[F, Transaction],
//  kafkaErrorProducer: KafkaErrorProducer[F],
  //scenarioConfiguration: ScenarioConfiguration
) {

  private val logger = Slf4jLogger.getLogger[F]
  implicit val InstantMeta: Meta[LocalDate] = Meta[String].imap(LocalDate.parse)(_.toString)

  def subscribe2 = {
    val kafkaProducer = new KafkaErrProduce(kafkaConfig)
    KafkaReceiver.create[F](kafkaConfig)(
      TransactionParser.parseJson,
      kafkaProducer.streamProduce,
      storeTransaction,
      amlRuleChecker.check)
//    logger.info("Subscribed to the Kafka Consumer...")
//    KafkaReceiver.create[F](kafkaConfig)(TransactionParser.parseJson(_), kafkaErrorProducer.produceOne(_), saveTransaction(_), saveTransaction(_))
  }
//  def subscribe = {
//    logger.info("Subscribed to the Kafka Consumer...")
//    val trnsStream = kafkaConsumer.stream.evalMap { chunk =>
//      createTransactions(chunk.map { case (update, _) => update })
//        .map(_ -> CommittableOffsetBatch.fromFoldable(chunk.map {
//          case (_, offset) => offset
//        }))
//    }
//    trnsStream.evalMap{
//      case (x,offsetBatch) => logger.info(s"$x messages commited") *> offsetBatch.commit.as(x)
//    }
//  }

//  private def checkForAlert(
//    transactions: Chunk[Transaction]
//  ): F[Int] = {
//    transactions.traverse(transaction => {
//      val matchedKeywords = scenarioConfiguration.keywords.filter(transaction.Description.contains(_))
//      if (!matchedKeywords.isEmpty) {
//        insertAlert("MatchedKeywords", matchedKeywords.mkString(","), transaction.Reference, transaction.OurIBAN.value)
//      } else if (scenarioConfiguration.highRiskCountries.contains(transaction.CountryCode)) {
//        insertAlert("HighRiskCountry", transaction.CountryCode, transaction.Reference, transaction.OurIBAN.value)
//      } else if (transaction.Amount > scenarioConfiguration.defaultMaxThreshold) {
//        insertAlert("ThresholdCheck", transaction.Amount.toString(), transaction.Reference, transaction.OurIBAN.value)
//      } else {
//        Sync[F].pure(0)
//      }
//    }
//    ).map(_.sumAll)
//  }


   def storeTransaction: Transaction => F[Unit] = (transaction: Transaction) => {
    val sqlInsert =
      """
           insert ignore into Transaction
        |(OurIBAN, TheirIBAN, Reference, TransactionCode, BookingDateTime,
        |DebitCredit, Amount, Currency, Description, CountryCode)
        |values (?,?,?,?,?,?,?,?,?,?);
         """.stripMargin

    Update[Transaction](sqlInsert)
      .run(transaction)
      .transact(xa)
      .map(_ => ())
      .handleErrorWith { err =>
        logger.error(err)(s"Failed to insert transaction in DB, ${err.getMessage}")
      }
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
   //   _ <- checkForAlert(transactions)
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
//
//object TransactionTopicSubscriber {
//  def apply[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Timer: Applicative](
//    xa: HikariTransactor[F],
//    kafkaConfig: KafkaConfig,
//    scenarioConfiguration: ScenarioConfiguration
//  ): Resource[F, TransactionTopicSubscriber[F]] = for {
//    trnsErrorProducer <- KafkaErrorProducer.apply[F](kafkaConfig)
//    transactionConsumer <- KafkaConsumer.apply[F, Transaction](kafkaConfig)
//  } yield new TransactionTopicSubscriber[F](xa, kafkaConfig,transactionConsumer, trnsErrorProducer)
//
//
//}