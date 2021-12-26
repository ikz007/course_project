package lv.scala.aml.common.dto

import io.circe._, io.circe.generic.semiauto._
final case class Questionnaire(
  QuestionnaireID: String,
  CustomerID: String,
  Country: String,
  MonthlyTurnover: BigDecimal,
  AnnualTurnover: BigDecimal,
  Reason: String,
  Active: Boolean = true
)

object Questionnaire {
  implicit val fooDecoder: Decoder[Questionnaire] = deriveDecoder[Questionnaire]
  implicit val fooEncoder: Encoder[Questionnaire] = deriveEncoder[Questionnaire]
}