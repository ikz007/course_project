package lv.scala.aml.database.repository

import cats.data.OptionT
import lv.scala.aml.common.dto.Questionnaire

trait QuestionnaireRepository[F[_]] extends BussinessObjectRepository[F, Questionnaire] {
  def getByCustomerID(customerID: String): F[List[Questionnaire]]
  def getById(id: String): OptionT[F, Questionnaire]
}
