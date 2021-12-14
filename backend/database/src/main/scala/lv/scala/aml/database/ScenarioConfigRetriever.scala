package lv.scala.aml.database

import cats.effect.Sync
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import lv.scala.aml.common.dto.Exceptions.FailedToRetrieve
import lv.scala.aml.common.dto.scenario.ScenarioConfiguration

class ScenarioConfigRetriever[F[_] : Sync](
  xa: HikariTransactor[F]
) {
  private val logger = Slf4jLogger.getLogger[F]
  def retrieveConfiguration = {
    for {
      highRiskCountrySetting <- selectFromSettingsTable("HighRiskCountries")
      hrc <- highRiskCountrySetting match {
        case Some(script) => Query(script).toFragment().query[String].to[List].transact(xa)
        case None => logger.info("Failed to retrieve high risk country setting...") *> Sync[F].raiseError(  FailedToRetrieve("HighRiskCountries"))
      }
      keywordSetting <- selectFromSettingsTable("TransactionKeyword")
      keyword <-  keywordSetting match {
        case Some(script) => Query(script).toFragment().query[String].to[List].transact(xa)
        case None => logger.info("Failed to retrieve high risk country setting...") *> Sync[F].raiseError (  FailedToRetrieve("TransactionKeyword"))
      }
    } yield ScenarioConfiguration(hrc, keyword)
  }

  private def selectFromSettingsTable(setting: String) =
    fr"select SettingSQL from ScenarioSetting where SettingName = $setting"
      .query[String].option.transact(xa)
}

object ScenarioConfigRetriever {
  def apply[F[_]: Sync](xa: HikariTransactor[F]) : ScenarioConfigRetriever[F] = {
    new ScenarioConfigRetriever[F](xa)
  }
}