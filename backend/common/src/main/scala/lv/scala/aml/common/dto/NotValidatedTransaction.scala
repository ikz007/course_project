package lv.scala.aml.common.dto

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.Instant

final case class NotValidatedTransaction(
  OurIBAN: String,
  TheirIBAN: String,
  Reference: String,
  TransactionCode: String,
  BookingDateTime: Instant,
  DebitCredit: String,
  Amount: BigDecimal,
  Currency: String,
  Description: String,
  CountryCode: String
)


object NotValidatedTransaction {
  implicit val transactionDecoder: Decoder[NotValidatedTransaction] = deriveDecoder[NotValidatedTransaction]
  implicit val transactionEncoder: Encoder[NotValidatedTransaction] = deriveEncoder[NotValidatedTransaction]
}