package lv.scala.aml.common.dto

import io.circe._
import io.circe.generic.semiauto._

import java.time.LocalDate


final case class Account(
  IBAN: IBAN,
  BBAN: Option[String],
  AccountType: Option[String],
  OpenDate: Option[LocalDate],
  CloseDate: Option[LocalDate],
  Status: String = "Active"
)

object Account {
  implicit val fooDecoder: Decoder[Account] = deriveDecoder[Account]
  implicit val fooEncoder: Encoder[Account] = deriveEncoder[Account]
}