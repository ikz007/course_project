package lv.scala.aml.common.dto

import java.util.Date

case class Relationship(
  CustomerID: BigInt,
  IBAN: String,
  StartDate: Date,
  EndDate: Date
)
