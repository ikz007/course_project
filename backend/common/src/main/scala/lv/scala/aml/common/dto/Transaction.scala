package lv.scala.aml.common.dto

import io.circe._
import io.circe.generic.semiauto._

import java.time.Instant

case class Transaction (
  OurIBAN: String,
  TheirIBAN: String,
  Reference: String,
  TransactionCode: String,
  BookingDateTime: String,
  DebitCredit: String,
  Amount: BigDecimal,
  Currency: String,
  Description: String,
  CountryCode: String
)

object Transaction {
  implicit val transactionDecoder: Decoder[Transaction] = deriveDecoder[Transaction]
  implicit val transactionEncoder: Encoder[Transaction] = deriveEncoder[Transaction]
}