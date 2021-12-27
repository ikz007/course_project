package lv.scala.aml.common.dto.parser

import cats.data.NonEmptyList
import io.circe.config.parser
import lv.scala.aml.common.dto.{InvalidMessage, NotValidatedTransaction, Transaction}
import cats.implicits._
import lv.scala.aml.common.dto.responses.ParsingError
import lv.scala.aml.common.dto.responses.ParsingError._

object TransactionParser {
  def parseJson: String => Either[InvalidMessage, Transaction] = (input: String) =>
    parser
      .parse(input)
      .flatMap(_.as[NotValidatedTransaction])
      .leftMap(_ => NonEmptyList.one(ParsingError("Transaction", FailedToParseJson)))
      .flatMap(trns => Transaction(trns).toEither)
      .leftMap( errs => InvalidMessage(input, errs.mkString_(",")))
}
