package prices.services.helpers
import org.http4s.headers._
import cats.effect._
import org.http4s._

object ProxyRequest {
  def apply[F[_]: Concurrent](token: String) =
    Request[F](
      method = Method.GET,
      headers = Headers(
        Authorization(Credentials.Token(AuthScheme.Bearer, token)),
        Accept(MediaType.application.json)
      )
    )
}
