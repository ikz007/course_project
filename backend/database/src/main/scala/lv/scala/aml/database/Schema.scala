package lv.scala.aml.database

import doobie.quill.DoobieContext.MySQL
import doobie.util.meta.Meta
import io.getquill.{CamelCase, MappedEncoding}
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{TransactionCode, TransactionType}

import java.time.{Instant, LocalDate}
import java.util.Date

trait Schema {
  val ctx: MySQL[CamelCase] with Decoders with Encoders
  implicit val instantDecoder: MappedEncoding[Date, Instant] = MappedEncoding[Date, Instant](_.toInstant)
  implicit val instanceEncoder: MappedEncoding[Instant, Date] = MappedEncoding[Instant, Date](Date.from)
  implicit val encodeLocalDate = MappedEncoding[LocalDate, String](_.toString())
  implicit val decodeLocalDate = MappedEncoding[String, LocalDate](LocalDate.parse(_))
  implicit val InstantMeta: Meta[LocalDate] = Meta[String].imap(LocalDate.parse)(_.toString)
  implicit val encodeTransactionCode = MappedEncoding[TransactionCode, String](_.value)
  implicit val decodeTransactionCode = MappedEncoding[String, TransactionCode](TransactionCode.withValue)
  implicit val encodeTransactionType = MappedEncoding[TransactionType, String](_.value)
  implicit val decodeTransactionType = MappedEncoding[String, TransactionType](TransactionType.withValue)
}
