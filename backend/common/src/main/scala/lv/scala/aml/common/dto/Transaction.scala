package lv.scala.aml.common.dto

import cats.data.Validated.Valid
import cats.data.ValidatedNel
import cats.implicits.{catsSyntaxOptionId, catsSyntaxTuple7Semigroupal, catsSyntaxTuple8Semigroupal, catsSyntaxValidatedId}
import io.circe._
import io.circe.generic.semiauto._
import lv.scala.aml.common.dto.responses.ParsingError
import lv.scala.aml.common.dto.responses.ParsingError.{InvalidFormatProvided, NotProvidedOrEmpty}

import java.time.{LocalDate, ZoneId}
import scala.util.Try

final case class Transaction (
  OurIBAN: IBAN,
  TheirIBAN: IBAN,
  Reference: String,
  TransactionCode: TransactionCode,
  BookingDateTime: LocalDate,
  DebitCredit: TransactionType,
  Amount: BigDecimal,
  Currency: String,
  Description: Option[String],
  CountryCode: String
)

object Transaction {
  implicit val transactionDecoder: Decoder[Transaction] = deriveDecoder[Transaction]
  implicit val transactionEncoder: Encoder[Transaction] = deriveEncoder[Transaction]
  private type ErrorsOr[T] = ValidatedNel[ParsingError, T]

  def apply(nVTransaction: NotValidatedTransaction): ValidatedNel[ParsingError, Transaction] = {
    val validateDebitCredit: ErrorsOr[TransactionType] = {
      TransactionType.withValueOpt(nVTransaction.DebitCredit) match {
        case Some(value) => Valid(value)
        case None => ParsingError("DebitCredit", InvalidFormatProvided).invalidNel
      }
    }

    val validateCurrency: ErrorsOr[String] =
      if(nVTransaction.Currency.matches("^[a-zA-Z]{3}$")) {
        Valid(nVTransaction.Currency)
      } else if (nVTransaction.Currency.trim.isEmpty) {
        ParsingError("Currency", NotProvidedOrEmpty).invalidNel
      } else {
        ParsingError("Currency", InvalidFormatProvided).invalidNel
      }

    val validateReference: ErrorsOr[String] =
      if(nVTransaction.Reference.trim.nonEmpty) {
        Valid(nVTransaction.Reference)
      } else  {
        ParsingError("Reference", InvalidFormatProvided).invalidNel
      }

    val validateCountryCode: ErrorsOr[String] =
      if(nVTransaction.CountryCode.matches("^[a-zA-Z]{2}$")) {
        Valid(nVTransaction.CountryCode)
      } else if (nVTransaction.CountryCode.trim.isEmpty) {
        ParsingError("CountryCode", NotProvidedOrEmpty).invalidNel
      } else {
        ParsingError("CountryCode", InvalidFormatProvided).invalidNel
      }

    val validateDate: ErrorsOr[LocalDate] =
      Try(LocalDate.ofInstant(nVTransaction.BookingDateTime, ZoneId.of("GMT"))).toOption match {
        case Some(value) => Valid(value)
        case None => ParsingError("BookingDateTime", InvalidFormatProvided).invalidNel
      }

    val validateTransactionCode: ErrorsOr[TransactionCode] =
      TransactionCode.withValueOpt(nVTransaction.TransactionCode) match {
        case Some(value) => Valid(value)
        case None => ParsingError("TransactionCode", InvalidFormatProvided).invalidNel
      }

    (
      IBANHandler.validate(nVTransaction.OurIBAN),
      IBANHandler.validate(nVTransaction.TheirIBAN),
      validateReference,
      validateCurrency,
      validateCountryCode,
      validateTransactionCode,
      validateDate,
      validateDebitCredit).mapN{
      case ( ourIban, theirIban, reference, currency, countryCode, trnsCode, trnsDate, debitCredit) =>
        Transaction(
          ourIban,
          theirIban,
          reference,
          trnsCode,
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