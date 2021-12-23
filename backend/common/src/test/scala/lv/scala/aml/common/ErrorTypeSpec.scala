package lv.scala.aml.common

import io.circe.Json
import io.circe.syntax._
import lv.scala.aml.common.dto.responses.ErrorType
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ErrorTypeSpec extends AnyWordSpec with Matchers with EitherValues {
  "Error type " should {
    "successfully parse error types" in {
      ErrorType.values.foreach{ error =>
        error.asJson == Json.fromString(error.value)
      }
    }
  }
}
