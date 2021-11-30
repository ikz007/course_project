package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.Customer

trait CustomerRepository[F[_]] extends BussinessObjectRepository[F, Customer]