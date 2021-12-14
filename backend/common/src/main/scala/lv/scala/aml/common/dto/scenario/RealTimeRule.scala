package lv.scala.aml.common.dto.scenario

import lv.scala.aml.common.dto.{Alert, Transaction}

object RealTimeRule {

  trait RealTimeRule {
    def check(transaction: Transaction): Option[RealTimeRule]
    def eval(transaction: Transaction): Option[Alert]
  }
  
  case class ThresholdRule(transaction: Transaction) extends RealTimeRule {
    override def check(transaction: Transaction): Option[RealTimeRule] = ???

    override def eval(transaction: Transaction): Option[Alert] = ???
  }

  
}