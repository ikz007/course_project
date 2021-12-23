package lv.scala.aml.common.dto.responses

import io.circe.generic.JsonCodec

@JsonCodec case class HttpResponse[T](success: Boolean, result: T)