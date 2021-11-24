package lv.scala.aml.common.dto

case class Transaction (
  OurIBAN: String,
  TheirIBAN: String,
  Reference: String,
  TransactionCode: String,
  DebitCredit: Char,
  Amount: BigDecimal,
  Currency: String,
  Description: String,
  CountryCode: String
)
