package lv.scala.aml.common.dto

import io.circe._
import io.circe.generic.semiauto._

import java.time.{Instant, LocalDate}

final case class Relationship(
  RelationshipID: String,
  CustomerID: String,
  IBAN: IBAN,
  StartDate: LocalDate,
  EndDate: Option[LocalDate]
)

object Relationship {
  implicit val fooDecoder: Decoder[Relationship] = deriveDecoder[Relationship]
  implicit val fooEncoder: Encoder[Relationship] = deriveEncoder[Relationship]
}