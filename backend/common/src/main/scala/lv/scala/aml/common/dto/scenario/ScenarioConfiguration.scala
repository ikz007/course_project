package lv.scala.aml.common.dto.scenario

case class ScenarioConfiguration(
  highRiskCountries  : List[String],
  keywords           : List[String],
  defaultMaxThreshold: BigDecimal = 5000
)
