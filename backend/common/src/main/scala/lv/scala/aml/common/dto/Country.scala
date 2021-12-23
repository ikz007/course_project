package lv.scala.aml.common.dto

import io.circe._, io.circe.generic.semiauto._
final case class Country(
  CountryISO: String,
  CountryName: String,
  HighRiskCountry: Boolean = false
)

object Country {
    implicit val fooDecoder: Decoder[Country] = deriveDecoder[Country]
    implicit val fooEncoder: Encoder[Country] = deriveEncoder[Country]
}