package lv.scala.aml.http

import cats.data.Validated
import lv.scala.aml.common.dto.IBAN
import org.http4s.{ParseFailure, QueryParamDecoder}

trait Helper {


  implicit val ibanDecoder: QueryParamDecoder[IBAN] = { param =>
    Validated
      .catchNonFatal(IBAN(param.value))
      .leftMap(t => ParseFailure(s"Failed to decode IBAN", t.getMessage))
      .toValidatedNel
  }
}
