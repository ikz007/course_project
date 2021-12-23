package lv.scala.aml.database.utils

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.{IBAN, Transaction}
import lv.scala.aml.common.dto.rules.AmlRule
import lv.scala.aml.common.dto.rules.AmlRule.{And, HighRiskCountryCheck, KeywordCheck, Rule, TransactionExceeds}
import lv.scala.aml.common.dto.scenario.ScenarioConfiguration


final case class AmlRuleChecker[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Timer: Applicative](
  xa: HikariTransactor[F],
  rules:Seq[AmlRule]
) {
  private val logger = Slf4jLogger.getLogger[F]

  def check(
    transaction: Transaction
  ): F[Unit]  = rules.map(flag(_, transaction)).sequence.void

  private def insertAlert(
    generate: Boolean,
    alertedCondition: String,
    alertedValue: String,
    transactionReference: String,
    subject: IBAN,
    scenarioName: String = "RealTime",
    subjectType: String = "Account"
  ): F[Unit] = {
    if(generate) {
      sql"""insert into Alert
        (Subject, SubjectType, TransactionReferences,
        AlertedCondition, AlertedValue, DateCreated,
        ScenarioName)
        values (${subject.value}, $subjectType, $transactionReference, $alertedCondition, $alertedValue, NOW(), $scenarioName);
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

  private def flag(
    rule:AmlRule,
    transaction:Transaction
  )
  //db objects or transactor
  : F[Unit] = rule match {
    case TransactionExceeds(amount) =>
      insertAlert(transaction.Amount > amount, "TransactionExceeds", transaction.Amount.toString(), transaction.Reference, transaction.OurIBAN)
    case HighRiskCountryCheck(countries) =>
      insertAlert(countries.contains(transaction.CountryCode), "HighRiskCountryCheck", transaction.CountryCode, transaction.Reference, transaction.OurIBAN)
    case KeywordCheck(keywords) =>
      insertAlert(keywords.exists(transaction.Description.contains(_)), "KeywordCheck", transaction.Description.getOrElse(""), transaction.Reference, transaction.OurIBAN)
  }
}
object AmlRuleChecker{
  def apply[F[_]: Sync: Logger: ContextShift: ConcurrentEffect: Timer: Applicative](
    xa: HikariTransactor[F],
    scenarioConfiguration: ScenarioConfiguration
  ):AmlRuleChecker[F] = {
    val rules = Seq(
      TransactionExceeds(scenarioConfiguration.defaultMaxThreshold),
      HighRiskCountryCheck(scenarioConfiguration.highRiskCountries),
      KeywordCheck(scenarioConfiguration.keywords)
    )
    new AmlRuleChecker[F](xa, rules)
  }
}