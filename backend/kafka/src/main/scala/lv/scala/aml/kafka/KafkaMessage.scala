package lv.scala.aml.kafka

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class KafkaMessage[A](requestId: String, message: A)
object KafkaMessage {
  implicit def decoder[A: Decoder]: Decoder[KafkaMessage[A]] = deriveDecoder
  implicit def encoder[A: Encoder]: Encoder[KafkaMessage[A]] = deriveEncoder
}
