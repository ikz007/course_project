package lv.scala.aml.database.dao
import doobie.implicits._

object DaoInit {

  def createDB =
    sql"""
         |CREATE DATABASE IF NOT EXISTS AML_Monitoring
       """.update
}
