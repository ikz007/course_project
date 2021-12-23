package lv.scala.aml.database

import doobie.implicits._
import doobie.util.meta.Meta
import lv.scala.aml.common.dto.{Account, Alert, Country, Customer, IBAN, Relationship, Transaction}

import java.time.LocalDate
/*
Type checking tables
 */
class MigrationsSpec extends DatabaseSpec {
  implicit val InstantMeta: Meta[LocalDate] = Meta[String].imap(LocalDate.parse)(_.toString)
  "Flyway migrations" should {
    "create the country table" in {
      check(
        sql"""SELECT * FROM Country""".query[Country]
      )
    }

    "create the alert table" in {
      check(
        sql"""SELECT * FROM Alert""".query[Alert]
      )
    }

    "create account table" in {
      check(
        sql"""SELECT * FROM Account""".query[Account]
      )
    }

    "select IBAN" in {
      check(
        sql"""SELECT IBAN FROM Account""".query[IBAN]
      )
    }

    "create the transaction table" in {
      check(
        sql"""SELECT * FROM Transaction""".query[Transaction]
      )
    }

    "create the customer table" in {
      check(
        sql"""SELECT * FROM Customer""".query[Customer]
      )
    }

    "create the relationship table" in {
      check(
        sql"""SELECT * FROM Relationship""".query[Relationship]
      )
    }

  }
}
