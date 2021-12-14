package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.Alert
import fs2.Stream

trait AlertRepository[F[_]] {
  def get: F[List[Alert]]
  def getById(id: Int): OptionT[F, Alert]
  def getCustomerAlerts(customerID : String): F[List[Alert]]
  def getAccountAlerts(IBAN: String): F[List[Alert]]
  def getStream: Stream[F, Alert]
}
