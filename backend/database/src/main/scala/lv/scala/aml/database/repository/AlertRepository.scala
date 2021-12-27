package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.{Alert, IBAN}

trait AlertRepository[F[_]] extends BussinessObjectRepository[F, Alert] with GetByIdRepository[F, Int, Alert] {
  def getCustomerAlerts(customerID : String): F[List[Alert]]
  def getAccountAlerts(iban: IBAN): F[List[Alert]]
  def getTransactionAlerts(reference: String): F[List[Alert]]
}
