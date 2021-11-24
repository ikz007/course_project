package lv.scala.aml.common.dto

case class Country(
  CountryISO: String,
  CountryName: String,
  HighRiskCountry: Boolean = false
)