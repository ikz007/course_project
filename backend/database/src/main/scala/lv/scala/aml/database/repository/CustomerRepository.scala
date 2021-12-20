package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.Customer

trait CustomerRepository[F[_]] extends BussinessObjectRepository[F, Customer] {
  def getById(id: String): OptionT[F, Customer]
}