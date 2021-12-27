package lv.scala.aml.database.repository

import lv.scala.aml.common.dto.Questionnaire

trait QuestionnaireRepository[F[_]] extends BussinessObjectRepository[F, Questionnaire] with GetByIdRepository[F, String, Questionnaire] {
  def getByCustomerID(customerID: String): F[List[Questionnaire]]
}
