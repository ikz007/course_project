package lv.scala.aml.http.services
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import lv.scala.aml.common.dto.Customer
import lv.scala.aml.database.repository.interpreter.CustomerRepositoryInterpreter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
class CustomerService [F[_]: Sync](
  customerRepositoryInterpreter: CustomerRepositoryInterpreter[F]
) extends Http4sDsl[F] {
  private def get: F[List[Customer]] = customerRepositoryInterpreter.get
  private def getById(customerID: String): OptionT[F, Customer] = customerRepositoryInterpreter.getById(customerID)
  private def update(customer: Customer): F[String] = customerRepositoryInterpreter.update(customer)

  // create response models
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @ GET -> Root / "customers" / "all" =>
      get.flatMap { case list => Ok(list.asJson)
      case Nil => NotFound()
      }
    case _ @ GET -> Root / "customers" / id =>
      getById(id).value.flatMap {
        case Some(customer) => Ok(customer.asJson)
        case None => NotFound("Such account does not exist")
      }
    case req @ PUT -> Root / "customers" =>
      req.decode[Customer] { acc =>
        update(acc).flatMap(_ => Accepted())
      }.handleErrorWith(e => BadRequest(e.getMessage))
  }
}

object CustomerService {
  def apply[F[_]: Sync](
    customerRepositoryInterpreter: CustomerRepositoryInterpreter[F]
  ): CustomerService[F] = new CustomerService[F](customerRepositoryInterpreter)
}
