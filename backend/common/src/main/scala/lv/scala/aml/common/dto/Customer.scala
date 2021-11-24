package lv.scala.aml.common.dto

import java.util.Date

case class Customer(
  CustomerID: String,
  CustomerName: String,
  BusinessType: String,
  MonthlyIncome: BigDecimal,
  Status: String = "Active",
  BirthDate: Date,
  PEP: Boolean = false,
  CountryOfBirth: String,
  CountryOfResidence: String
)
