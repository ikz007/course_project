package lv.scala.aml.database

import doobie.implicits._
import lv.scala.aml.common.dto._
/*
Type checking tables
 */
class MigrationsSpec extends DatabaseSpec with DoobieSchema {
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
    "create the rules table" in {
      check(
        sql"""SELECT * FROM RuleTable""".query[(Int, String)]
      )
    }
  }
}
