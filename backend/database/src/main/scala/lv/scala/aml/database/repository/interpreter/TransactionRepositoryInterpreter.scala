package lv.scala.aml.database.repository.interpreter

//import cats.Applicative
import cats.data.OptionT
import cats.effect.Sync
import lv.scala.aml.common.dto.IBAN
//import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext.MySQL
//import io.chrisdavenport.log4cats.Logger
import io.getquill.CamelCase
import io.getquill.context.jdbc.{Decoders, Encoders}
import lv.scala.aml.common.dto.{Relationship, Transaction}
import lv.scala.aml.database.Schema
import lv.scala.aml.database.repository.TransactionRepository
import doobie.implicits._
import cats.syntax.all._
//import lv.scala.aml.common.dto.responses.KafkaErrorMessage
//import lv.scala.aml.config.KafkaConfig
//import lv.scala.aml.kafka.Serdes._
//import lv.scala.aml.kafka.{KafkaConsumer, KafkaErrorProducer}

class TransactionRepositoryInterpreter [F[_]: Sync](
  xa: HikariTransactor[F],
//  kafkaConsumer: KafkaConsumer[F, Transaction],
//  kafkaErrorProducer: KafkaErrorProducer[F, KafkaErrorMessage],
  override val ctx: MySQL[CamelCase] with Decoders with Encoders
) extends TransactionRepository[F] with Schema{
  import ctx._

  override def getCustomerTransactions(customerID: String): F[List[Transaction]] = run(quote {
    query[Transaction]
      .join(query[Relationship]).on(_.OurIBAN == _.IBAN)
      .filter(_._2.CustomerID == lift(customerID))
      .map{ case (transaction, _) => transaction}
  }).transact(xa)

  override def getAccountTransactions(iban: IBAN): F[List[Transaction]] = run(quote {
    query[Transaction]
      .filter(_.OurIBAN == lift(iban))
  }).transact(xa)

  override def get: F[List[Transaction]] = run(quote {
    query[Transaction]
  }).transact(xa)

  override def getById(id: String): OptionT[F, Transaction] =
    OptionT(run(quote {
      query[Transaction].filter(_.Reference == lift(id))
    }).transact(xa).map(_.headOption))

  override def update(model: Transaction): F[Unit] = run(quote {
    query[Transaction]
      .filter(_.Reference == lift(model.Reference))
      .update(lift(model))
  }).transact(xa).void
}

//object TransactionRepositoryInterpreter {
//  def apply[F[_]: ConcurrentEffect : ContextShift : Timer : Applicative](
//    xa: HikariTransactor[F],
//    kafkaConfig: KafkaConfig,
//    ctx: MySQL[CamelCase] with Decoders with Encoders
//  ): Resource[F, TransactionRepositoryInterpreter[F]] = for {
//    transactionConsumer <- KafkaConsumer.apply[F, Transaction](kafkaConfig)
//    trnsErrorProducer <- KafkaErrorProducer.apply[F, KafkaErrorMessage](kafkaConfig)
//  } yield new TransactionRepositoryInterpreter[F](xa, transactionConsumer, trnsErrorProducer,ctx)
//
//
//}
