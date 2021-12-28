package lv.scala.aml.common

import io.circe.Json
import io.circe.syntax._
import lv.scala.aml.common.dto.{TransactionCode, TransactionType}
import lv.scala.aml.common.dto.responses.ErrorType
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EnumTypeSpec extends AnyWordSpec with Matchers with EitherValues {
  "EnumTypeSpec test" should {
    "successfully parse error types" in {
      ErrorType.values.foreach{ error =>
        error.asJson must be(Json.fromString(error.value))
      }
    }
    "successfully parse Transaction code" in {
      TransactionCode.values.foreach{ trnsCode =>
        trnsCode.asJson must be(Json.fromString(trnsCode.value))
      }
    }
    "successfully parse Transaction type" in {
      TransactionType.values.foreach{ trnsType =>
        trnsType.asJson must be(Json.fromString(trnsType.value))
      }
    }
  }
}
