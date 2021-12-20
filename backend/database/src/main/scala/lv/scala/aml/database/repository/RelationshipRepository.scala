package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.{Account, Customer, IBAN, Relationship}

trait RelationshipRepository[F[_]] extends BussinessObjectRepository[F, Relationship] {
  def getRelatedCustomers(iban: IBAN): F[List[Customer]]
  def getRelatedAccounts(customerID: String): F[List[Account]]
  def getById(id: String): OptionT[F, Relationship]
}
