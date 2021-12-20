package lv.scala.aml.common.dto.rules

sealed trait AmlRule

object AmlRule{
  final case class TransactionExceeds(amount:BigDecimal) extends AmlRule
  final case class And(left:AmlRule, right:AmlRule) extends AmlRule
  final case class Or(left:AmlRule, right:AmlRule) extends AmlRule
  final case class HighRiskCountryCheck(countryList:List[String]) extends  AmlRule
  final case class KeywordCheck(keywords: List[String]) extends AmlRule

  final case class Rule(ruleName: String, alertedValue: String, generate: Boolean)
  //
  //  def ruleToJson(rule:AmlRule): String  = ???
  //
  //  def jsonToRule(jsonRule: String): AmlRule  = ???
  //https://circe.github.io/circe/codecs/adt.html
}