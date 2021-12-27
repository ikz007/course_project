package lv.scala.aml.kafka

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import fs2.concurrent.Queue
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.parser.TransactionParser
import lv.scala.aml.common.dto.{InvalidMessage, NotValidatedTransaction, Transaction}
import lv.scala.aml.config.Config
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.Instant
import scala.concurrent.ExecutionContext


class KafkaErrorProducerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with EitherValues{
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  "Kafka" - {
    "should receive a message when published" in {
      val nvTrns = NotValidatedTransaction("FR1420041010050500014", "FR1420041010050500014", "24", "CRDT", Instant.now(), "D", BigDecimal(23.457), "EUR", "Descr", "LV")
      val trns = Transaction(nvTrns).getOrElse(fail("Failed to parse"))
      (for {
        q <- Queue.unbounded[IO, Unit]
        ref <- Ref.of[IO, Option[Transaction]](none)
        _ <- KafkaProducerSpec(testConfig.kafka).streamProduce(nvTrns)
        _ <- KafkaReceiver.create[IO](testConfig.kafka)(TransactionParser.parseJson, _ => IO.unit, tr => ref.set(tr.some), _ => IO.unit)
        .through(q.enqueue).compile.drain.start.bracket(_ => q.dequeue1)(_.cancel)
        res <- ref.get
      } yield res).asserting(_ shouldBe trns.some)

    }
    "should receive an Error message when published" in {
      val invalidMessage = InvalidMessage("error", "error")
      (for {
        testConfig <- Config.load[IO]()
        _ <- KafkaErrProduce[IO](testConfig.kafka).streamProduce(invalidMessage)
        consumerSettings <- IO.delay(
          ConsumerSettings[IO, Unit, String]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers(testConfig.kafka.bootstrapServers)
            .withGroupId(testConfig.kafka.groupId)
            .withClientId(testConfig.kafka.clientId)
        )
        res <- fs2.kafka.KafkaConsumer.stream(consumerSettings)
          .evalTap(_.subscribeTo(testConfig.kafka.producerTopic))
          .flatMap(_.stream)
          .take(1)
          .compile
          .lastOrError
      } yield res).asserting(_.record.value shouldBe "{\"message\":\"error\",\"err\":\"error\"}")
    }
  }
}
