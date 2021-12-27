package lv.scala.aml.common.dto.rules

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._
import cats.syntax.all._

import scala.concurrent.duration.{Duration, FiniteDuration}

sealed trait AmlRule

object AmlRule{

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("aml_rule")

  implicit val encoder: Encoder[AmlRule] = deriveEncoder[AmlRule]
  implicit val decoder: Decoder[AmlRule] = deriveDecoder[AmlRule]
  implicit val finiteDurationEncoder: Encoder[FiniteDuration] = Encoder.encodeString.contramap(_.toString)
  implicit val finiteDurationDecoder: Decoder[FiniteDuration] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(Duration(str).asInstanceOf[FiniteDuration])
      .leftMap(_ => s"cannot parse FiniteDuration from $str")
  }

  final case class TransactionExceeds(amount:BigDecimal) extends AmlRule
  final case class And(left:AmlRule, right:AmlRule) extends AmlRule
  final case class Or(left:AmlRule, right:AmlRule) extends AmlRule
  final case class HighRiskCountryCheck(countryList:List[String]) extends  AmlRule
  final case class KeywordCheck(keywords: List[String]) extends AmlRule
  final case class UnexpectedBehavior(timesBigger: Int, duration: FiniteDuration) extends AmlRule
  final case class UndeclaredCountry(duration: FiniteDuration) extends AmlRule

  def jsonToRule: String => Option[AmlRule] =
    decode[AmlRule](_).toOption

  def ruleToJson: AmlRule => String = _.asJson.noSpaces
}