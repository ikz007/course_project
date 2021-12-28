package lv.scala.aml.database

import doobie.util.meta.Meta
import lv.scala.aml.common.dto.{TransactionCode, TransactionType}

import java.time.LocalDate

trait DoobieSchema {
  implicit val InstantMeta: Meta[LocalDate] = Meta[String].imap(LocalDate.parse)(_.toString)
  implicit val TransactionCodeMeta = Meta[String].imap(TransactionCode.withValue)(_.value)
  implicit val TransactionTypeMeta = Meta[String].imap(TransactionType.withValue)(_.value)
}
