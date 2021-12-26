package lv.scala.aml.common.dto

import io.circe.generic.JsonCodec
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
  implicit val accountDecoder: Decoder[Account] = deriveDecoder[Account]
  implicit val accountEncoder: Encoder[Account] = deriveEncoder[Account]
}