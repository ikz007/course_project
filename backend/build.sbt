import Dependencies._

lazy val root = (project in file("."))
  .aggregate(
    http,
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
      Http4s.BlazeServer,
      Http4s.Dsl,
      Http4s.Circe,
      Http4s.BlazeClient
    )
  )

lazy val database = (project in file("database"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      Database.MySQLConnector,
      Database.FlyWay,
      Database.DoobieH2,
      Database.DoobieCore,
      Database.DoobieSpec,
      Database.DoobieHikari,
      Circe.CirceCore,
      Circe.CirceConfig,
      Circe.CirceGeneric
    )
  )

lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      Circe.CirceCore,
      Circe.CirceConfig,
      Circe.CirceGeneric,
      Cats.CatsEffect
    )
  )

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases"
)