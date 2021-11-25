package lv.scala.aml.http.services

import cats.data.OptionT
import cats.effect.Sync
import lv.scala.aml.common.dto.Account
import lv.scala.aml.database.repository.interpreter.AccountRepositoryInterpreter

class AccountService[F[_]: Sync](
  accountInterpreter: AccountRepositoryInterpreter[F]
) {
  def get: F[List[Account]] = accountInterpreter.get
  def getById(iban: String): OptionT[F, Account] = accountInterpreter.getById(iban)
  def update(account: Account): F[String] = accountInterpreter.update(account)
}
