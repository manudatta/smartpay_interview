package prices.routes


import cats.effect._
import org.http4s._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import cats.effect.IO


import prices.routes.protocol._
import prices.services.PriceService
import prices.data.Price

final case class PriceRoutes[F[_]: Sync](priceService: PriceService[F]) extends Http4sDsl[F] {

  val prefix = "/prices"

  implicit val priceResponseEncoder = jsonEncoderOf[F, PriceResponse]
  
  object KindQueryParamMatcher extends QueryParamDecoderMatcher[String]("kind")

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? KindQueryParamMatcher(kind)
      => println("In routes")
		  Ok(priceService.get(kind))
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}
