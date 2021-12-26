package lv.scala.aml.database.utils

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.rules.AmlRule
import lv.scala.aml.common.dto.rules.AmlRule._
import lv.scala.aml.common.dto.{IBAN, Transaction}


final case class AmlRuleChecker[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Applicative](
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