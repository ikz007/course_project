package lv.scala.aml.kafka

import cats.effect.{Sync}
import fs2.kafka.{Deserializer, Serializer}
import io.circe.jawn.JawnParser
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

import java.nio.ByteBuffer

/*
  custom Encoder/Decoder for types not supported by fs2.kafka
 */
object Serdes {
  type Attempt[T] = Either[Throwable, T]

  implicit def encodingSer[F[_]: Sync, A: Encoder]: Serializer[F, A] =
    Serializer.string[F].contramap(_.asJson.noSpaces)

  implicit def decodingSer[F[_]: Sync, A: Decoder]: Deserializer[F, Either[Throwable, A]] =
    Deserializer[F].map { bytes =>
      new JawnParser().decodeByteBuffer[A](ByteBuffer.wrap(bytes))
    }
}
