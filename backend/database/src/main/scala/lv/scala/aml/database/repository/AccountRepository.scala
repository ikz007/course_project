package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.Account

trait AccountRepository[F[_]] {
  def get:F[List[Account]]
  def getById(iban: String): OptionT[F, Account]
  def update(account: Account): F[String]
}
