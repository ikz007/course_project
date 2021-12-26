package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.{IBAN, Transaction}

trait TransactionRepository[F[_]] extends BussinessObjectRepository[F, Transaction]{
  def getCustomerTransactions(customerID : String): F[List[Transaction]]
  def getAccountTransactions(iban: IBAN): F[List[Transaction]]
  def getById(id: String): OptionT[F, Transaction]
  def getQuestTransactions(questId: String): F[List[Transaction]]
}
