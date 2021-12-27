package lv.scala.aml.http

import cats.effect.IO
import cats.implicits.{catsSyntaxOptionId, none}
import lv.scala.aml.common.dto.{Account, Alert}
import lv.scala.aml.common.dto.responses.ErrorType.FailedToParse
import lv.scala.aml.common.dto.responses.HttpResponse
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.implicits._
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import lv.scala.aml.common.dto.Account.{accountDecoder, accountEncoder}

import scala.concurrent.duration.DurationInt
class HttpSpec extends AnyFreeSpec with Matchers with EitherValues{
  "HttpServices should" - {
    "validate IBAN" in {
      val invalidResponseIO = Main.makeRouter(testTransactor).run(
        Request(method = Method.GET, uri = uri"/accounts/LV123")
      )
      validate[HttpResponse[String]](
        invalidResponseIO,
        Status.BadRequest,
        HttpResponse(false, FailedToParse.value).some
      )
      val validResponseIO = Main.makeRouter(testTransactor).run(
        Request(method = Method.GET, uri = uri"/accounts/FR1420041010050500013")
      )
      validate[HttpResponse[Account]](
        validResponseIO,
        Status.Ok,
        none
      )
    }
    "validate alert ID" in {
      val invalidAlertResponseIO = Main.makeRouter(testTransactor).run(
        Request(method = Method.GET, uri = uri"/alerts/ABC")
      )
      validate[HttpResponse[String]](
        invalidAlertResponseIO,
        Status.BadRequest,
        HttpResponse(false, FailedToParse.value).some
      )
      val validAlertResponseIO = Main.makeRouter(testTransactor).run(
        Request(method = Method.GET, uri = uri"/alerts/1")
      )
      validate[HttpResponse[Alert]](
        validAlertResponseIO,
        Status.NotFound,
        none
      )
    }
  }
  private def validate[T](
    actualResponseIO: IO[Response[IO]],
    expectedStatus: Status,
    expectedBody: Option[T]
  )(implicit decoder: EntityDecoder[IO, T]) = (for {
    actualResponse <- actualResponseIO
    _ <- IO(actualResponse.status shouldBe expectedStatus)
    _ <- expectedBody match {
      case Some(response) => IO(actualResponse.as[T].map(_ shouldBe response) )
      case None => IO.unit
    }
  } yield ()).unsafeRunTimed(10.seconds)
}
