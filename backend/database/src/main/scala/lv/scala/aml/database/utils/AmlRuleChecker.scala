package lv.scala.aml.database.utils

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.Transaction
import lv.scala.aml.common.dto.rules.AmlRule
import lv.scala.aml.common.dto.rules.AmlRule._

import java.time.Instant
import scala.concurrent.duration.FiniteDuration


final case class AmlRuleChecker[F[_]: Sync : Logger: ContextShift: ConcurrentEffect: Applicative](
  xa: HikariTransactor[F],
  rules:Seq[AmlRule]
) {
  private val logger = Slf4jLogger.getLogger[F]

  def check(
    transaction: Transaction
  ): F[Unit]  =
    rules.map(rule =>
      for {
        r <- flag(rule, transaction)
        _ <- insertAlert(r, ruleToJson(rule), transaction)
      } yield ()
    ).sequence.void

  private def insertAlert(
    generate: Boolean,
    alertedCondition: String,
    transaction: Transaction
  ): F[Unit] = {
    if(generate) {
      sql"""insert into Alert
        (Iban, TransactionReferences,
        AlertedCondition, DateCreated)
        values (${transaction.OurIBAN}, ${transaction.Reference}, $alertedCondition, NOW());
        """.update.withUniqueGeneratedKeys[Int]("AlertId")
        .transact(xa)
        .map(_ => ())
        .handleErrorWith(err =>
          logger.error(err)(s"Failed to create alert in DB, ${err.getMessage}")
        )
    } else {
      Sync[F].unit
    }
  }

  private def getAverageTransactionAmount(transaction: Transaction, duration: FiniteDuration) =
    for {
      timeNow <- Sync[F].delay(Instant.now().minusMillis(duration.toMillis))
      res <- sql"""
        SELECT avg(Amount)
        FROM Transaction
        WHERE BookingDateTime >= ${timeNow.toString}
          AND OurIBAN = ${transaction.OurIBAN}
          AND Reference <> ${transaction.Reference}
         GROUP BY OurIBAN
       """.query[BigDecimal].option.transact(xa)
    } yield res

  private def checkForDeclaredThreshold(transaction: Transaction) =
    for {
      res <- sql"""
          SELECT q.MonthlyTurnover
          FROM Questionnaire q
          INNER JOIN Relationship r
            ON q.CustomerID = r.CustomerID
          WHERE r.IBAN = ${transaction.OurIBAN}
            AND q.Country = ${transaction.CountryCode}
            AND q.Active = 1
           """.query[BigDecimal].option.transact(xa)
    } yield res

  private def checkForTransactionSum(transaction: Transaction, duration: FiniteDuration) =
    for {
      timeNow <- Sync[F].delay(Instant.now().minusMillis(duration.toMillis))
      res <-
        sql"""
          SELECT sum(Amount)
          FROM Transaction
          WHERE BookingDateTime >= ${timeNow.toString}
          AND OurIBAN = ${transaction.OurIBAN}
          AND CountryCode = ${transaction.CountryCode}
           """.query[BigDecimal].option.transact(xa)
    } yield res

  private[database] def flag(
    rule:AmlRule,
    transaction:Transaction
  ): F[Boolean] = rule match {
    case TransactionExceeds(amount) =>
      Sync[F].delay(transaction.Amount > amount)
    case HighRiskCountryCheck(countries) =>
      Sync[F].delay(countries.contains(transaction.CountryCode))
    case KeywordCheck(keywords) =>
      Sync[F].delay(keywords.exists(transaction.Description.contains(_)))
    case UnexpectedBehavior(timesBigger, duration) =>
      for {
        avgAmount <- getAverageTransactionAmount(transaction, duration)
      } yield avgAmount match {
        case Some(avg) =>
          transaction.Amount / avg >= timesBigger
        case None => false
      }
    case UndeclaredCountry(duration: FiniteDuration) =>
      for {
        declared <- checkForDeclaredThreshold(transaction)
        total <- checkForTransactionSum(transaction, duration)
        res <- Sync[F].delay(declared match {
          case Some(decl) => total match {
            case Some(tot) => tot > decl
            case None => transaction.Amount > decl
          }
          case None => true
        })
      } yield res
    case And(left, right) => for {
      isLeft <- flag(left, transaction)
      isRight <- flag(right, transaction)
    } yield isLeft && isRight
    case Or(left, right) => for {
      isLeft <- flag(left, transaction)
      isRight <- flag(right, transaction)
    } yield isLeft || isRight
  }
}
object AmlRuleChecker{
  def apply[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Applicative](
    xa: HikariTransactor[F]
  ):F[AmlRuleChecker[F]] = {
    for {
      rules <- retrieveRulesFromDB(xa)
      parsed <- Sync[F].delay(rules.flatMap(jsonToRule))
    } yield new AmlRuleChecker[F](xa, parsed)
  }

  private def retrieveRulesFromDB[F[_]: Sync](xa: HikariTransactor[F]): F[Seq[String]] =
    sql"""SELECT RuleJson FROM RuleTable""".query[String].to[Seq].transact(xa)
}