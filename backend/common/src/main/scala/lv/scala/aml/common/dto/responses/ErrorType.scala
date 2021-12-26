package lv.scala.aml.common.dto.responses

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}


sealed abstract class ErrorType(override val value: String) extends StringEnumEntry

case object ErrorType extends StringEnum[ErrorType] with StringCirceEnum[ErrorType] {
  val values = findValues
  case object BusinessObjectNotFound extends ErrorType("not_found")
  case object UpdateFailed extends ErrorType("update_failed")
  case object FailedToParse extends ErrorType("failed_to_parse")
}

