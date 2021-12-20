package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.Country

trait CountryRepository[F[_]] extends BussinessObjectRepository[F, Country] {
  def getById(id: String): OptionT[F, Country]
}
