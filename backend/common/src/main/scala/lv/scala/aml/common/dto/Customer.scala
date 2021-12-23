package lv.scala.aml.common.dto

import io.circe._
import io.circe.generic.semiauto._

import java.time.LocalDate

final case class Customer(
  CustomerID: String,
  CustomerName: String,
  BusinessType: Option[String],
  MonthlyIncome: BigDecimal,
  Status: String = "Active",
  BirthDate: LocalDate,
  PEP: Boolean = false,
  CountryOfBirth: String,
  CountryOfResidence: String
)

object Customer {
  implicit val fooDecoder: Decoder[Customer] = deriveDecoder[Customer]
  implicit val fooEncoder: Encoder[Customer] = deriveEncoder[Customer]
}
