package lv.scala.aml.common.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDate

final case class Alert (
  AlertId: Int,
  Iban: IBAN,
  TransactionReferences: String,
  AlertedCondition: String,
  DateCreated: LocalDate
)

object Alert {
  implicit val fooDecoder: Decoder[Alert] = deriveDecoder[Alert]
  implicit val fooEncoder: Encoder[Alert] = deriveEncoder[Alert]
}