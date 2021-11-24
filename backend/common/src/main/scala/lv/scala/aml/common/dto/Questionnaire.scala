package lv.scala.aml.common.dto
case class Questionnaire(
  CustomerID: BigInt,
  Country: String,
  MonthlyTurnover: BigDecimal,
  AnnualTurnover: BigDecimal,
  Reason: String
)
