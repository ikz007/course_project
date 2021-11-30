package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.Country

trait CountryRepository[F[_]] extends BussinessObjectRepository[F, Country]
