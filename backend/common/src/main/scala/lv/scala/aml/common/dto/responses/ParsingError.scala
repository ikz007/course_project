package lv.scala.aml.common.dto.responses

import cats.Show
import lv.scala.aml.common.dto.responses.ParsingError.FailReason

case class ParsingError(field: String, error: FailReason)

object ParsingError {
  sealed trait FailReason
  case object NotProvidedOrEmpty extends FailReason
  case object InvalidFormatProvided extends FailReason
  case object FailedToParseJson extends FailReason

  implicit val showParsingError: Show[ParsingError] = Show.show{
    case ParsingError(field, NotProvidedOrEmpty) => s"Field `$field` was not provided or empty"
    case ParsingError(field, InvalidFormatProvided) => s"Format of the field `$field` is invalid"
    case ParsingError(_, FailedToParseJson) => s"Failed to parse object"
  }
}