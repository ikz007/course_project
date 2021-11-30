package lv.scala.aml.database.repository.interpreter

import cats.data.OptionT
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.Questionnaire
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.QuestionnaireRepository
import doobie.implicits._
import cats.syntax.all._

class QuestionnaireRepositoryInterpreter[F[_]: Sync: Logger](
  xa: HikariTransactor[F],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends QuestionnaireRepository[F] with Schema{
  import ctx._

  override def getById(questionnaireID: String): OptionT[F, Questionnaire] =
    OptionT(run(quote {
      query[Questionnaire].filter(_.QuestionnaireID == lift(questionnaireID))
    }).transact(xa).map(_.headOption))

  override def get: F[List[Questionnaire]] = run(quote {
    query[Questionnaire]
  }).transact(xa)

  override def update(model: Questionnaire): F[String] =
    run(quote {
      query[Questionnaire]
        .filter(_.QuestionnaireID == lift(model.QuestionnaireID))
        .update(lift(model))
    }).transact(xa).as(model.QuestionnaireID)

  override def getByCustomerID(customerID: String): F[List[Questionnaire]] =
    run(quote {
      query[Questionnaire].filter(_.CustomerID == lift(customerID))
    }).transact(xa)
}
