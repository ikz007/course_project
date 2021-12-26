package lv.scala.aml

import cats.effect.IO
import lv.scala.aml.config.Config

import scala.concurrent.ExecutionContext

package object kafka {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  def getConfig = Config.load[IO]()

  lazy val testConfig: Config = getConfig.unsafeRunSync()
}
