package prices.routes

import cats.implicits._
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import prices.routes.protocol._
import prices.services.InstanceKindService
import prices.services.InstanceKindService.Exception.APICallFailure

final case class InstanceKindRoutes[F[_]: Sync](instanceKindService: InstanceKindService[F]) extends Http4sDsl[F] {

  val prefix = "/instance-kinds"

  implicit val instanceKindResponseEncoder = jsonEncoderOf[F, List[InstanceKindResponse]]

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root =>
      instanceKindService
        .getAll()
        .flatMap(kinds => Ok(kinds.map(k => InstanceKindResponse(k))))
        .handleErrorWith(error =>
          error match {
            case APICallFailure(msg) => InternalServerError(msg)
            case _                   => InternalServerError("Unknown error")
          }
        )
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}
