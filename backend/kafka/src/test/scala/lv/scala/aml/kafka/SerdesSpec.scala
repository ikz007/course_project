package lv.scala.aml.kafka

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.kafka.Headers
import lv.scala.aml.common.dto.InvalidMessage
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class SerdesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with EitherValues{
  "Serdes should " - {
    "do a successful round trip of (de)serialization" in {
      val invalidMessage = InvalidMessage("err1", "error1")

      (for {
          serialized <- Serdes.encodingSer[IO, InvalidMessage].serialize(
            "errorTopic", Headers.empty, invalidMessage
          )
          deserialized <- Serdes.decodingSer[IO, InvalidMessage].deserialize(
            "errorTopic", Headers.empty, serialized
          )
        } yield deserialized).asserting(_.getOrElse(fail("Failed to parse")) shouldBe invalidMessage)

    }
  }
}
