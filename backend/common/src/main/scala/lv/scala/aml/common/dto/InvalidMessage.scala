package lv.scala.aml.common.dto

import io.circe.generic.JsonCodec

@JsonCodec case class InvalidMessage(message: String, err: String)