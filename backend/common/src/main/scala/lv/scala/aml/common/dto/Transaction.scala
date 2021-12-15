package lv.scala.aml.common.dto

import io.circe._
import io.circe.generic.semiauto._

import java.time.Instant


// Lets use correct types. For instance BookingDateTime:LocalDateTime, for IBANS create case class that contains IBAN number
// One functionality that you could add is datatype validation during parsing/ object creation

case class IBAN(value:String)
case class Transaction (
  OurIBAN: IBAN,
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