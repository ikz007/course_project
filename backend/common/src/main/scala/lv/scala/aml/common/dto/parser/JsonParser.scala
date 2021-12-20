package lv.scala.aml.common.dto.parser

import cats.data.NonEmptyList
import io.circe.config.parser
import lv.scala.aml.common.dto.{InvalidMessage, NotValidatedTransaction, Transaction}
import cats.implicits._

object TransactionParser {
  def parseJson: String => Either[InvalidMessage, Transaction] = (input: String) =>
    parser
      .parse(input)
      .flatMap(_.as[NotValidatedTransaction])
      .leftMap(e => NonEmptyList.one(e.getMessage))
      .flatMap(trns => Transaction(trns).toEither)
      .leftMap( errs => InvalidMessage(input, errs.mkString_(",")))
}
