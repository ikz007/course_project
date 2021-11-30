package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.Account

trait AccountRepository[F[_]] extends BussinessObjectRepository[F, Account]