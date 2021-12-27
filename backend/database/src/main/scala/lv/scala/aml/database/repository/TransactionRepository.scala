package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.{IBAN, Transaction}

trait TransactionRepository[F[_]] extends BussinessObjectRepository[F, Transaction] with GetByIdRepository[F, String, Transaction] {
  def getCustomerTransactions(customerID : String): F[List[Transaction]]
  def getAccountTransactions(iban: IBAN): F[List[Transaction]]
  def getQuestTransactions(questId: String): F[List[Transaction]]
}
