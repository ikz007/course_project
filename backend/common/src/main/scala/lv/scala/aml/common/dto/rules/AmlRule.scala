package lv.scala.aml.common.dto.rules

import cats.Applicative
import cats.effect.Sync
import cats.implicits.toTraverseOps
import cats.syntax.flatMap._
import cats.syntax.functor._
import lv.scala.aml.common.dto.Transaction

sealed trait AmlRule

object AmlRule{
  final case class TransactionExceeds(amount:BigDecimal) extends AmlRule
  final case class And(left:AmlRule, right:AmlRule) extends AmlRule
  final case class Or(left:AmlRule, right:AmlRule) extends AmlRule
  final case class ToCountry(country:String) extends  AmlRule
  final case class FromCountry(country:String) extends  AmlRule
  //...


  def flag[F[_] :Sync :Applicative](rule:AmlRule, transaction:Transaction)
                                  //db objects or transactor
  : F[Boolean] = rule match {
    case TransactionExceeds(amount) =>  Sync[F].delay(transaction.Amount > amount)
    case And(left,right) =>  for {
      isLeft <- flag(left,transaction)
      isRight <- flag(right,transaction)
    } yield isLeft && isRight
    case FromCountry(country) =>  Sync[F].delay(transaction.CountryCode == country)
    //...
  }

  def ruleToJson(rule:AmlRule): String  = ???

  def jsonToRule(jsonRule: String): AmlRule  = ???
//https://circe.github.io/circe/codecs/adt.html
}

final case class AmlRuleChecker[F[_]:Sync]( rules:Seq[AmlRule]) {
  def check(transaction: Transaction): F[Boolean]  = rules.map(AmlRule.flag(_, transaction)).sequence.map( _.exists(identity))
}
object AmlRuleChecker{
  def apply[F[_]:Sync]():AmlRuleChecker[F] = ???
}