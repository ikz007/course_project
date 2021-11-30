package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.{Account, Customer, Relationship}

trait RelationshipRepository[F[_]] extends BussinessObjectRepository[F, Relationship] {
  def getRelatedCustomers(IBAN: String): F[List[Customer]]
  def getRelatedAccounts(customerID: String): F[List[Account]]
}
