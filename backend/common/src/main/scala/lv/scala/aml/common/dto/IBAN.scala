package lv.scala.aml.common.dto

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder}
import lv.scala.aml.common.dto.responses.ParsingError
import lv.scala.aml.common.dto.responses.ParsingError.InvalidFormatProvided

@JsonCodec case class IBAN(value: String) extends AnyVal

object IBAN {
  implicit val ibanDecoder: Decoder[IBAN] = deriveDecoder[IBAN]
  implicit val ibanEncoder: Encoder[IBAN] = Encoder.encodeString.contramap[IBAN](_.value)
}

object IBANHandler {
  def validate(input: String): ValidatedNel[ParsingError, IBAN] =
    (if(input.matches("^[A-Z]{2}\\d{2}(?:\\d{4}){3}\\d{4}(?:\\d\\d?)?$")) {
      input.validNel
    } else {
     ParsingError("IBAN", InvalidFormatProvided).invalidNel
    }).map[IBAN](IBAN.apply)
}