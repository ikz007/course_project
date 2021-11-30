package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.Transaction

trait TransactionRepository[F[_]] extends BussinessObjectRepository[F, Transaction]{
  def getCustomerTransactions(customerID : String): F[List[Transaction]]
  def getAccountTransactions(IBAN: String): F[List[Transaction]]
}
