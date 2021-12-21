package lv.scala.aml.common.dto

import cats.data.{NonEmptyList, ValidatedNel}
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxValidatedId
import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

@JsonCodec case class IBAN(value: String) extends AnyVal

object IBAN {
  implicit val ibanDecoder: Decoder[IBAN] = deriveDecoder[IBAN]
  implicit val ibanEncoder: Encoder[IBAN] = Encoder.encodeString.contramap[IBAN](_.value)
}

object IBANHandler {
  def validate(input: String): ValidatedNel[String, IBAN] =
    (if(input.matches("^[A-Z]{2}\\d{2}(?:\\d{4}){3}\\d{4}(?:\\d\\d?)?$")) {
      input.validNel
    } else {
      "Failed to validate the IBAN".invalidNel
    }).map[IBAN](IBAN.apply)
}