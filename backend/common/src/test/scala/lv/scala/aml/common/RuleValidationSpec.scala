package lv.scala.aml.common

import lv.scala.aml.common.dto.rules.AmlRule._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

class RuleValidationSpec extends AnyWordSpec with Matchers with EitherValues{
  "AmlRule" should {
    "successfully parsing TransactionExceeds" in {
      val transactionExceeds = TransactionExceeds(5000)
      val transactionJson = "{\"TransactionExceeds\":{\"amount\":5000}}"
      ruleToJson(transactionExceeds) must be(transactionJson)
      jsonToRule(transactionJson) match {
        case Some(value) => value must be(transactionExceeds)
        case None => fail("Failed to parse")
      }
    }
    "successfully parsing complex rules" in {
      val andRule = And(TransactionExceeds(5000), KeywordCheck(List("terror", "north korea")))
      val andJson = "{\"And\":{\"left\":{\"TransactionExceeds\":{\"amount\":5000}},\"right\":{\"KeywordCheck\":{\"keywords\":[\"terror\",\"north korea\"]}}}}"
      ruleToJson(andRule) must be(andJson)
      jsonToRule(andJson) match {
        case Some(value) => value must be(andRule)
        case None => fail("Failed to parse")
      }
    }
    "successfully parsing UnexpectedBehavior" in {
      val unexpectedBehavior = UnexpectedBehavior(3, 30.days)
      val behaviorJson = "{\"UnexpectedBehavior\":{\"timesBigger\":3,\"duration\":\"30 days\"}}"
      ruleToJson(unexpectedBehavior) must be(behaviorJson)
      jsonToRule(behaviorJson) match {
        case Some(value) => value must be(unexpectedBehavior)
        case None => fail("Failed to parse")
      }
    }

    "successfully parsing transaction to/from undeclared country scenario" in {
      val undeclaredCountry = UndeclaredCountry(30.days)
      val transactionExceeds = TransactionExceeds(1000)
      val combined = And(undeclaredCountry, transactionExceeds)
      val combinedJson = "{\"And\":{\"left\":{\"UndeclaredCountry\":{\"duration\":\"30 days\"}},\"right\":{\"TransactionExceeds\":{\"amount\":1000}}}}"
      ruleToJson(combined) must be(combinedJson)
      jsonToRule(combinedJson) match {
        case Some(value) => value must be(combined)
        case None => fail("Failed to parse")
      }
    }
  }
}
