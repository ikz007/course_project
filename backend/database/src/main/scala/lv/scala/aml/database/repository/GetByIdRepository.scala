package lv.scala.aml.database.repository

import cats.data.OptionT

trait GetByIdRepository [F[_], K, V] {
  def getById(id: K):OptionT[F, V]
}
