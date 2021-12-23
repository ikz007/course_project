package lv.scala.aml.common.dto

import cats.data.Validated.{Invalid, Valid}
import lv.scala.aml.common.dto.IBAN
import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits.{catsSyntaxOptionId, catsSyntaxTuple7Semigroupal}
import io.circe._
import io.circe.generic.semiauto._

import java.time.{LocalDate, ZoneId, ZoneOffset}
import scala.util.Try

case class Transaction (
  OurIBAN: IBAN,
  TheirIBAN: IBAN,
  Reference: String,
  TransactionCode: String,
  BookingDateTime: LocalDate,
  DebitCredit: String,
  Amount: BigDecimal,
  Currency: String,
  Description: Option[String],
  CountryCode: String
)

object Transaction {
  implicit val transactionDecoder: Decoder[Transaction] = deriveDecoder[Transaction]
  implicit val transactionEncoder: Encoder[Transaction] = deriveEncoder[Transaction]
  private type ErrorsOr[T] = ValidatedNel[String, T]

  def apply(nVTransaction: NotValidatedTransaction): ValidatedNel[String, Transaction] = {
    val validateDebitCredit: ErrorsOr[String] =
      if (nVTransaction.DebitCredit != "C" || nVTransaction.DebitCredit != "D") {
        Valid(nVTransaction.DebitCredit)
      } else {
        Invalid(NonEmptyList.one(s"Unknown data for DebitCredit: ${nVTransaction.DebitCredit}"))
      }

    val validateCurrency: ErrorsOr[String] =
      if(nVTransaction.Currency.matches("^[a-zA-Z]{3}$")) {
        Valid(nVTransaction.Currency)
      } else if (nVTransaction.Currency.trim.isEmpty) {
        Invalid(NonEmptyList.one("Data is not provided for the field: Currency"))
      } else {
        Invalid(NonEmptyList.one(s"Unknown data for Currency: ${nVTransaction.Currency}"))
      }

    val validateReference: ErrorsOr[String] =
      if(!nVTransaction.Reference.trim.isEmpty) {
        Valid(nVTransaction.Reference)
      } else  {
        Invalid(NonEmptyList.one("Data is not provided for the field: Reference"))
      }

    val validateCountryCode: ErrorsOr[String] =
      if(nVTransaction.CountryCode.matches("^[a-zA-Z]{2}$")) {
        Valid(nVTransaction.CountryCode)
      } else if (nVTransaction.CountryCode.trim.isEmpty) {
        Invalid(NonEmptyList.one("Data is not provided for the field: CountryCode"))
      } else {
        Invalid(NonEmptyList.one(s"Unknown data for CountryCode: ${nVTransaction.DebitCredit}"))
      }

    val validateDate: ErrorsOr[LocalDate] =
      Try(LocalDate.ofInstant(nVTransaction.BookingDateTime, ZoneId.of(ZoneOffset.UTC.getId()))).toOption match {
        case Some(value) => Valid(value)
        case None => Invalid(NonEmptyList.one(s"Couldnt parse the date: ${nVTransaction.BookingDateTime}"))
      }

    (
      IBANHandler.validate(nVTransaction.OurIBAN),
      IBANHandler.validate(nVTransaction.TheirIBAN),
      validateReference,
      validateCurrency,
      validateCountryCode,
      validateDate,
      validateDebitCredit).mapN{
      case ( ourIban, theirIban, reference, currency, countryCode, trnsDate, debitCredit) =>
        Transaction(
          ourIban,
          theirIban,
          reference,
          nVTransaction.TransactionCode,
          trnsDate,
          debitCredit,
          nVTransaction.Amount,
          currency,
          nVTransaction.Description.some,
          countryCode
        )
    }
  }
}