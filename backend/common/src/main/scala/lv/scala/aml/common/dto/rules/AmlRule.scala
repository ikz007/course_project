package lv.scala.aml.common.dto.rules

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

sealed trait AmlRule

object AmlRule{

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("aml_rule")

  implicit val encoder: Encoder[AmlRule] = deriveEncoder[AmlRule]
  implicit val decoder: Decoder[AmlRule] = deriveDecoder[AmlRule]

  final case class TransactionExceeds(amount:BigDecimal) extends AmlRule
  final case class And(left:AmlRule, right:AmlRule) extends AmlRule
  final case class Or(left:AmlRule, right:AmlRule) extends AmlRule
  final case class HighRiskCountryCheck(countryList:List[String]) extends  AmlRule
  final case class KeywordCheck(keywords: List[String]) extends AmlRule

  final case class Rule(ruleName: String, alertedValue: String, generate: Boolean)

  def jsonToRule: String => Option[AmlRule] =
    decode[AmlRule](_).toOption

  def ruleToJson: AmlRule => String = _.asJson.noSpaces
}