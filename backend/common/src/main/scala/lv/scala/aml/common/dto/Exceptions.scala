package lv.scala.aml.common.dto

import scala.util.control.NoStackTrace

object Exceptions {
  final case class FailedToRetrieve(settingName: String)
    extends RuntimeException(s"Failed to retrieve the following list $settingName") with NoStackTrace
}
