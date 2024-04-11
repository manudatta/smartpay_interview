package prices.routes

import cats.implicits._
import cats.effect._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import prices.routes.protocol._
import prices.services.PriceService
import org.http4s.HttpRoutes

final case class PriceRoutes[F[_]: Sync](priceService: PriceService[F]) extends Http4sDsl[F] {

  val prefix = "/prices"

  implicit val priceResponseEncoder = jsonEncoderOf[F, PriceResponse]

  object KindQueryParamMatcher extends QueryParamDecoderMatcher[String]("kind")

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? KindQueryParamMatcher(kind) =>
      priceService
        .get(kind)
        .flatMap(Ok(_))
        .handleErrorWith(error =>
          error match {
            case PriceService.Exception.APICallFailure(msg)       => InternalServerError(msg)
            case PriceService.Exception.InstanceKindNotFound(msg) => InternalServerError(msg + s" kind=$kind")
            case _                                                => InternalServerError("Unknown error")
          }
        )
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}
