package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.{Alert, IBAN}
import fs2.Stream

trait AlertRepository[F[_]] {
  def get: F[List[Alert]]
  def getById(id: Int): OptionT[F, Alert]
  def getCustomerAlerts(customerID : String): F[List[Alert]]
  def getAccountAlerts(iban: IBAN): F[List[Alert]]
  def getTransactionAlerts(reference: String): F[List[Alert]]
}
