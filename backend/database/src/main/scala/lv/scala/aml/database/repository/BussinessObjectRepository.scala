package lv.scala.aml.database.repository

import cats.data.OptionT

trait BussinessObjectRepository[F[_], T] {
  def get: F[List[T]]
  def update(model: T): F[String]
  def getById(id: String): OptionT[F, T]
}
