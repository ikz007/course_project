package lv.scala.aml.common.dto

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class TransactionCode(override val value: String) extends StringEnumEntry

case object TransactionCode extends StringEnum[TransactionCode] with StringCirceEnum[TransactionCode] {
  val values = findValues

  case object InternationalTransaction extends TransactionCode("INTT")
  case object CashWithdrawal extends TransactionCode("WTHD")
  case object CreditTransfer extends TransactionCode("CRDT")
}