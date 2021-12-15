package lv.scala.aml.database

import cats.effect._
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import lv.scala.aml.config.DBConfig

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.ExecutionContext

object Database {

  def newThreadFactory(name: String): ThreadFactory =
    new ThreadFactory {
      val ctr = new AtomicInteger(0)

      def newThread(r: Runnable): Thread = {
        val back = new Thread(r, s"$name-pool-${ctr.getAndIncrement()}")
        back.setDaemon(true)
        back
      }
    }

  final case class TransactorConfig(
    dbConfig: DBConfig,
    poolName: String = "AML-Hikari-Pool",
    driver: String = "com.mysql.cj.jdbc.Driver", //why not from config?
    hikariConnectionThreads: Int = 8
  ) {
    lazy val hikariDataSource = {
      val hikariConfig = new HikariConfig()
      hikariConfig.setJdbcUrl(dbConfig.url)
      hikariConfig.setDriverClassName(driver)
      hikariConfig.setUsername(dbConfig.username)
      hikariConfig.setPassword(dbConfig.password)
      hikariConfig.setMaximumPoolSize(dbConfig.poolSize)
      hikariConfig.setPoolName(poolName)
      hikariConfig.setLeakDetectionThreshold(30 * 1000)
      hikariConfig.setAutoCommit(false)
      new HikariDataSource(hikariConfig)
    }

    val connectionEC: ExecutionContext =
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(hikariConnectionThreads,
          newThreadFactory("db-connection")
        ))

    val transactionEC: ExecutionContext =
      ExecutionContext.fromExecutor(
        Executors.newCachedThreadPool(
          newThreadFactory("db-transaction")
        )
      )

    val transactionBlocker: Blocker = {
      Blocker.liftExecutionContext(transactionEC)
    }
  }

  def buildTransactor(
    config: TransactorConfig
  )(implicit cs: ContextShift[IO]): HikariTransactor[IO] =
    HikariTransactor.apply[IO](
      config.hikariDataSource,
      config.connectionEC,
      config.transactionBlocker
    )

  def buildTransactorResource(
    config: TransactorConfig
  )(implicit cs: ContextShift[IO]) = {
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.dbConfig.url,
      config.dbConfig.username,
      config.dbConfig.password,
      config.connectionEC,
      config.transactionBlocker
    )
  }
}
