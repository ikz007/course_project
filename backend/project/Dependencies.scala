import sbt._

object Dependencies {

  private val mokitoVersion = "1.16.49"
  private val catsVersion = "2.6.1"
  private val catsEffectVersion = "2.5.4"
  private val http4sVersion = "0.21.25"
  private val scalaTestVersion = "3.2.10"
  private val scalaMockVersion = "5.1.0"
  private val kafkaVersion = "1.8.0"
  private val circeVersion = "0.14.1"
  private val circeConfig = "0.8.0"
  private val doobieVersion = "0.9.4"
  private val slf4jVersion = "1.7.32"
  private val logbackClassic = "1.2.7"
  private val log4cats = "1.1.1"
  private val pureConfigVersion = "0.17.0"
  private val mysqlConnector = "8.0.25"
  private val flywayVersion = "8.0.2"
  private val quillVersion = "3.5.2"
  private val junitVersion = "4.12"
  private val catsEffectTestVersion = "0.5.4"
  private val munitCatsVersion = "1.0.0"
  private val munitVersion = "0.7.29"
  private val h2Version = "2.0.202"
  private val enumeratumVersion = "1.7.0"

  object Cats {
    val CatsCore = "org.typelevel" %% "cats-core" % catsVersion
    val CatsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  }

  object Circe {
    val CirceCore = "io.circe" %% "circe-core" % circeVersion
    val CirceGeneric = "io.circe" %% "circe-generic" % circeVersion
    val CirceConfig = "io.circe" %% "circe-config" % circeConfig
    val enumeratorCirce = "com.beachape" %% "enumeratum-circe" % enumeratumVersion
  }

  object Enum {
    val enumerator = "com.beachape" %% "enumeratum"% enumeratumVersion
  }

  object Database {
    val DoobieCore = "org.tpolecat" %% "doobie-core" % doobieVersion
    val DoobieH2 = "org.tpolecat" %% "doobie-h2" % doobieVersion
    val DoobieHikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
    val DoobieSpec = "org.tpolecat" %% "doobie-specs2" % doobieVersion
    val DoobieQuill = "org.tpolecat" %% "doobie-quill" % doobieVersion
    val MySQLConnector = "mysql" % "mysql-connector-java" % mysqlConnector
    val FlyWay = "org.flywaydb" % "flyway-core" % flywayVersion
    val Quill = "io.getquill" %% "quill-jdbc" % quillVersion
    val h2DB = "com.h2database" % "h2" % h2Version % Test
  }

  object Http4s {
    val BlazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
    val BlazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion
    val Circe = "org.http4s" %% "http4s-circe" % http4sVersion
    val Dsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  }

  object Kafka {
    val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % kafkaVersion
  }

  object Logging {
    val Sl4j = "org.slf4j" % "slf4j-simple" % slf4jVersion
    val Sl4core = "io.chrisdavenport" %% "log4cats-core" % log4cats
    val LogBack = "ch.qos.logback" % "logback-classic" % logbackClassic
    val Sl4cats = "io.chrisdavenport" %% "log4cats-slf4j" % log4cats
  }

  object PureConfig {
    val Core = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
    val CatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion
  }

  object ScalaTest {
    val WordSpec = "org.sclatest" %% "scalatest-wordspec" % scalaTestVersion
    val ShouldMatchers = "org.scalatest" %% "scalatest-shouldmatchers" % scalaTestVersion
    val ScalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    val JUnitTest = "junit" % "junit" % junitVersion
    val MockitoTest = "org.mockito" %% "mockito-scala-scalatest" % mokitoVersion % Test
    val catsEffectTest = "com.codecommit" %% "cats-effect-testing-scalatest" % catsEffectTestVersion % Test
    val munitCatsTest = "org.typelevel" %% "munit-cats-effect-2" % munitCatsVersion % "test"
    val munitTest = "org.scalameta" %% "munit" % munitVersion % "test"
    val doobieScalaTest = "org.tpolecat" %% "doobie-scalatest" % doobieVersion
  }
}
