package lv.scala.aml.database

import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

object DbInit {

  def initialize(xa: HikariTransactor[IO]): IO[MigrateResult] = {
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
