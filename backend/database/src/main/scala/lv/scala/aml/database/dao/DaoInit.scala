package lv.scala.aml.database.dao
import cats.effect.IO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

object DaoInit {

  def createDB =
    sql"""
         CREATE DATABASE IF NOT EXISTS AML_Monitoring;
       """.update

  def initialize(xa: HikariTransactor[IO]): IO[MigrateResult] = {
    println("java.")
    IO.delay {
      val m: FluentConfiguration = Flyway.configure
        .dataSource(xa.kernel)
        .group(true)
        .outOfOrder(false)
        .table("FlywaySchemaHistory")
        .baselineOnMigrate(true)

      m.load().migrate()
    }
  }

}
