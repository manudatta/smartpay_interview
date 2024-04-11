package prices.services

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import io.circe.generic.auto._


import org.http4s.client.Client
import prices.services.helpers.ProxyRequest
import prices.services.PriceService.Exception._
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
          case _ =>  APICallFailure("Error in building proxy url").raiseError[F, PriceResponse]
        }
      }
	
	private def createMsg(status: Status) = s"Failed status_code=${status.code}  message=${Option(status.reason).getOrElse("unknown reason")}"

    private def getAndHandleResponse(response: Response[F]): F[PriceResponse] =
      response.status match {
        case Status.Ok =>
          response.asJsonDecode[ProxyPriceResponse].map( x => PriceResponse(x.kind,x.price))
        case status @ Status.NotFound  => InstanceKindNotFound(createMsg(status)).raiseError[F, PriceResponse]
        case status_other          => APICallFailure(createMsg(status_other)).raiseError[F, PriceResponse]
      }


    override def get(kind: String): F[PriceResponse] =
      proxy(kind)
  }

}
