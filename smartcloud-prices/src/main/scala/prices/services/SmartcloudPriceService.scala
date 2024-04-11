package prices.services

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import io.circe.generic.auto._


import prices.data._
import org.http4s.client.Client
import prices.services.helpers.ProxyRequest
import prices.services.InstanceKindService.Exception.APICallFailure
import prices.routes.protocol.ProxyPriceResponse
import prices.routes.protocol.PriceResponse

object SmartcloudPriceService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Concurrent](httpClient: Resource[F, Client[F]], config: Config): PriceService[F] = new SmartcloudPriceService(httpClient, config)

  private final class SmartcloudPriceService[F[_]: Concurrent](
      httpClient: Resource[F, Client[F]],
      config: Config
  ) extends PriceService[F] {


    val getAllUri = s"${config.baseUri}/instances"

    def proxy(kind: String): F[PriceResponse] =
      httpClient.use { client =>
        val request = ProxyRequest(config.token)
        Uri.fromString(s"${getAllUri}/${kind}").toOption match {
          case Some(finalUri) => client.run(request.withUri(finalUri)).use(getAndHandleResponse)
        }
      }
	
	private def createMsg(status: Status) = s"Failed status_code=${status.code}  message=${Option(status.reason).getOrElse("unknown reason")}"

    private def getAndHandleResponse(response: Response[F]): F[PriceResponse] =
      response.status match {
        case Status.Ok =>
          response.asJsonDecode[ProxyPriceResponse].map( x => {println(x)
          PriceResponse(x.kind,x.price)})
      }


    override def get(kind: String): F[PriceResponse] =
      proxy(kind)
  }

}
