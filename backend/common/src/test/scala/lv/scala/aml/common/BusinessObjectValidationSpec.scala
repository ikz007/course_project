package lv.scala.aml.common

import cats.data.Validated
import cats.implicits.{catsSyntaxOptionId, none}
import lv.scala.aml.common.dto.parser.TransactionParser
import lv.scala.aml.common.dto.{IBAN, IBANHandler, NotValidatedTransaction, Transaction}
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDate, ZoneId, ZoneOffset}
class BusinessObjectValidationSpec extends AnyWordSpec with Matchers with EitherValues {
  "Business objects" should {
    "successfully parse transaction" in {
      val transactionJson = "{ \"OurIBAN\": \"DE89370400440532013000\",\"TheirIBAN\": \"FR1420041010050500013\", \"Reference\": \"21\", \"BookingDateTime\": \"2021-11-04T14:19:54.736Z\", \"TransactionCode\": \"CRDT\", \"DebitCredit\": \"D\", \"Amount\": 777433.39, \"Currency\": \"EUR\", \"Description\": \"123\", \"CountryCode\": \"KR\"}"
      val parse = TransactionParser.parseJson(transactionJson)
      val transaction = parse.getOrElse(fail(parse.toString))
      succeed
    }
    "pass incorrect json" in {
      val transactionJson = "abc"
      val parse = TransactionParser.parseJson(transactionJson)
      parse match {
        case Left(_) => succeed
        case Right(value) => fail(value.toString)
      }
    }
    "retrieve transactions reference" in {
      val transactionJson = "{ \"OurIBAN\": \"DE89370400440532013000\",\"TheirIBAN\": \"FR1420041010050500013\", \"Reference\": \"21\", \"BookingDateTime\": \"2021-11-04T14:19:54.736Z\", \"TransactionCode\": \"CRDT\", \"DebitCredit\": \"D\", \"Amount\": 777433.39, \"Currency\": \"EUR\", \"Description\": \"123\", \"CountryCode\": \"KR\"}"
      val parse = TransactionParser.parseJson(transactionJson)
      val transaction = parse.getOrElse(fail(parse.toString))
      transaction.Reference must be("21")
    }

    "IBAN successfully parsed" in {
      val iban1 = "DE89370400440532013000"
      val iban2 = "FR1420041010050500013"
      IBANHandler.validate(iban1).toOption must be(IBAN(iban1).some)
      IBANHandler.validate(iban2).toOption must be(IBAN(iban2).some)
    }

    "IBAN parsing failed" in {
      val iban1 = "89370654400440532013000"
      val iban2 = "LV7420041010050500"
      IBANHandler.validate(iban1).toOption must be(none)
      IBANHandler.validate(iban2).toOption must be(none)
    }

    "nvTransaction to Transaction" in {
      val localDate = LocalDate.now().atStartOfDay(ZoneId.of(ZoneOffset.UTC.getId()))
      val nvTrns = NotValidatedTransaction("FR1420041010050500013", "FR1420041010050500013", "24", "CRDT", localDate.toInstant, "D", BigDecimal(23.457), "EUR", "Descr", "LV")
      Transaction(nvTrns) match {
        case Validated.Valid(tr) =>
          tr.Reference must be(nvTrns.Reference)
          tr.BookingDateTime must be(localDate.toLocalDate)
        case Validated.Invalid(e) => fail(e.toString())
      }
    }
  }
}
