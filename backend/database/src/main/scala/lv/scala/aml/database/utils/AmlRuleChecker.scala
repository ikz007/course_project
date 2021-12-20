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
  ): F[Unit]  = rules.map(flag(_, transaction)).sequence.map(_.traverse( rule =>
  insertAlert(rule.generate, rule.ruleName, rule.alertedValue, transaction.Reference, transaction.OurIBAN)
  ))

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
  : F[Rule] = rule match {
    case TransactionExceeds(amount) => Sync[F].delay(Rule("TransactionExceeds", transaction.Amount.toString(), transaction.Amount > amount))
//    case And(left,right) =>  for {
//      first <- flag(left,transaction)
//      second <- flag(right,transaction)
//    } yield Rule(s"${first.ruleName};${second.ruleName}",s"${first.value};${second.value}", first.generate && second.generate)
    case HighRiskCountryCheck(countries) => Sync[F].delay(Rule("HighRiskCountryCheck", transaction.CountryCode, countries.contains(transaction.CountryCode)))
    case KeywordCheck(keywords) => Sync[F].delay(Rule("KeywordCheck", transaction.Description, keywords.exists(transaction.Description.contains(_))))
    case _ => Sync[F].delay(Rule("", "", false))
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