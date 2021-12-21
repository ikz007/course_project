import Dependencies._

lazy val root = (project in file("."))
  .aggregate(
    http,database, common
  )
  .settings(
    name := "scala-aml",
    inThisBuild(
      List(
        organization := "lv.scala.aml",
        scalaVersion := "2.13.7",
        scalacOptions ++= Seq(
          "-deprecation",
          "-feature",
          "-language:higherKinds",
          "-unchecked",
          "-Ylog-classpath",
          "-Ymacro-annotations",
          "-Xlint:_",
          "-Xlint:-byname-implicit"
        )
      )
    )
  )

lazy val http = (project in file("http"))
  .dependsOn(common % "compile->compile;test->test", database % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      Cats.CatsCore,
      Cats.CatsEffect,
      Database.DoobieQuill,
      Logging.Sl4j,
      Logging.Sl4cats,
      Logging.Sl4core,
      Http4s.BlazeServer,
      Http4s.Dsl,
      Http4s.Circe,
      Http4s.BlazeClient
    )
  )

lazy val database = (project in file("database"))
  .dependsOn(common % "compile->compile;test->test", kafka % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      Database.MySQLConnector,
      Database.FlyWay,
      Database.DoobieH2,
      Database.DoobieCore,
      Database.DoobieSpec,
      Database.DoobieHikari,
      Database.DoobieQuill,
      Database.Quill,
      Circe.CirceCore,
      Circe.CirceConfig,
      Circe.CirceGeneric,
      Logging.Sl4j,
      Logging.Sl4cats,
      Logging.Sl4core
    )
  )

lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      Circe.CirceCore,
      Circe.CirceConfig,
      Circe.CirceGeneric,
      Cats.CatsEffect,
      Http4s.Circe,
      ScalaTest.ScalaTest
    )
  )

lazy val kafka = (project in file("kafka"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++=Seq(
      Kafka.fs2Kafka,
      Cats.CatsCore,
      Cats.CatsEffect,
      Database.DoobieQuill,
      Logging.Sl4j,
      Logging.Sl4cats,
      Logging.Sl4core
//      Kafka.fs2KafkaVulkan,
//      Kafka.kafkaAvro
    )
  )

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases"
)