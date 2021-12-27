package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.{Account, Customer, IBAN, Relationship}

trait RelationshipRepository[F[_]] extends BussinessObjectRepository[F, Relationship] with GetByIdRepository[F, String, Relationship] {
  def getRelatedCustomers(iban: IBAN): F[List[Customer]]
  def getRelatedAccounts(customerID: String): F[List[Account]]
}
