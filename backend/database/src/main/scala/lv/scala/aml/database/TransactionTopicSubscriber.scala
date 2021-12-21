package lv.scala.aml.database

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.syntax.all._
import doobie.Update
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.meta.Meta
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.Transaction
import lv.scala.aml.common.dto.parser.TransactionParser
import lv.scala.aml.config.KafkaConfig
import lv.scala.aml.database.utils.AmlRuleChecker
import lv.scala.aml.kafka.{KafkaErrProduce, KafkaReceiver}

import java.time.LocalDate

class TransactionTopicSubscriber[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Timer: Applicative](
  xa: HikariTransactor[F],
  kafkaConfig: KafkaConfig,
  amlRuleChecker: AmlRuleChecker[F]
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
  }

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
}