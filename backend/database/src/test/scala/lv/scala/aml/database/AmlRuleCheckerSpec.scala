package lv.scala.aml.database

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.parser.TransactionParser
import lv.scala.aml.common.dto.rules.AmlRule.TransactionExceeds
import lv.scala.aml.database.utils.AmlRuleChecker
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.ExecutionContext

class AmlRuleCheckerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with EitherValues {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  "Aml rule checker" - {
    "Transaction exceeds scenario. Alert should be generated" in {
     val trnsJson = "{ \"OurIBAN\": \"DE89370400440532013000\",\"TheirIBAN\": \"FR1420041010050500013\", \"Reference\": \"2\", \"BookingDateTime\": \"2021-11-04T14:19:54.736Z\", \"TransactionCode\": \"CRDT\", \"DebitCredit\": \"D\", \"Amount\": 777433.39, \"Currency\": \"EUR\", \"Description\": \"123\", \"CountryCode\": \"KR\"}"
      val transaction = TransactionParser.parseJson(trnsJson).getOrElse(fail("Failed to parse"))
      val transactionExceeds = TransactionExceeds(5000)
      (for {
        ruleChecker <- IO(new AmlRuleChecker[IO](testTransactor, Seq(transactionExceeds)))
        res <- ruleChecker.flag(transactionExceeds, transaction)
      } yield res).asserting(_ shouldBe true)
    }
  }
}
