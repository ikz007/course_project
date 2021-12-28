package lv.scala.aml.common.dto

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class TransactionType(override val value: String) extends StringEnumEntry

case object TransactionType extends StringEnum[TransactionType] with StringCirceEnum[TransactionType] {
  val values = findValues
  case object Debit extends TransactionType("D")
  case object Credit extends TransactionType("C")
}