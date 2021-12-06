package lv.scala.aml.common.dto.responses

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class KafkaErrorMessage(
  error: String,
  orgMessage: String
)

object KafkaErrorMessage {
  implicit val kafkaErrorDecoder: Decoder[KafkaErrorMessage] = deriveDecoder[KafkaErrorMessage]
  implicit val kafkaErrorEncoder: Encoder[KafkaErrorMessage] = deriveEncoder[KafkaErrorMessage]
}