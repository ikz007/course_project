package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.{Account, IBAN}

trait AccountRepository[F[_]] extends BussinessObjectRepository[F, Account] {
  def getById(id: IBAN): OptionT[F, Account]
}