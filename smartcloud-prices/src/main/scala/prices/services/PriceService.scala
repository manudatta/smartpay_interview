package prices.services

import scala.util.control.NoStackTrace

import prices.routes.protocol.PriceResponse

trait PriceService[F[_]] {
  def get(kind: String): F[PriceResponse]
}

object PriceService {

  sealed trait Exception extends NoStackTrace
  object Exception {
    case class APICallFailure(message: String) extends Exception
  }

}
