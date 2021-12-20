package lv.scala.aml.common.dto

import io.circe._, io.circe.generic.semiauto._
import java.time.Instant


case class Account(
  IBAN: IBAN,
  BBAN: String,
  AccountType: String,
  OpenDate: Instant,
  CloseDate: Instant,
  Status: String = "Active"
)

object Account {
  implicit val fooDecoder: Decoder[Account] = deriveDecoder[Account]
  implicit val fooEncoder: Encoder[Account] = deriveEncoder[Account]
}