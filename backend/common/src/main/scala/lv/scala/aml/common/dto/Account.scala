package lv.scala.aml.common.dto

import java.util.Date

case class Account(
  IBAN: String,
  BBAN: String,
  AccountType: String,
  OpenDate: Date,
  CloseDate: Date = new Date(),
  Status: String = "Active"
)
